from types import SimpleNamespace as N
import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).parents[1] / 'src'))
from gifts import Gifts
from TikTokLive.events import GiftEvent
from TikTokLive.proto.custom_proto import ExtendedGift


def event(message=1, **kwargs):
    values=dict(gift=N(name='Hand Heart', diamond_count=100), streaking=False,
                gift_id=123, group_id=0, user=N(id=42), common=N(msg_id=message))
    values.update(kwargs)
    return N(**values)


def test_exact_gift_and_deduplication():
    g=Gifts()
    assert g.event(event(), 100)
    assert not g.event(event(), 106)
    assert not g.event(event(2), 104)
    assert not g.event(event(2), 110)  # cooldown events never replay
    assert g.event(event(3), 110)
    assert g.sequence == 2
    assert not g.snapshot(116)['events']


def test_wrong_or_unidentifiable_gifts():
    g=Gifts()
    for e in (event(gift=None), event(streaking=True),
              event(gift=N(name='Heart', diamond_count=100)),
              event(gift=N(name='Hand Heart', diamond_count=99)),
              event(gift_id=0), event(message=0)):
        assert not g.event(e, 100)
    assert g.sequence == 0
    assert g.event(event(gift=N(name='Hand Hearts', diamond_count=100)), 100)


def test_streak_identity_and_quantity_do_not_multiply():
    g=Gifts()
    assert g.event(event(group_id=66, repeat_count=1000), 100)
    assert not g.event(event(message=2, group_id=66), 110)
    assert g.sequence == 1


def test_bounded_memory_and_new_session():
    g=Gifts()
    for i in range(2200):
        g.event(event(i+1), i*6)
    assert len(g.events)==32 and len(g.seen)==2048
    assert Gifts().session != g.session


def test_installed_gift_event_structure():
    # Genuine installed-library protobuf event, not an invented property surface.
    from TikTokLiveProto.v3.webcast.shared.message import CommonMessageData
    e=GiftEvent(gift_id=123, gift=ExtendedGift(name='Hand Heart', diamond_count=100, type=1),
                common=CommonMessageData(msg_id=75), repeat_count=10, repeat_end=0)
    g=Gifts()
    assert not g.event(e,100)
    e.repeat_end=1
    assert g.event(e,101)
    assert not g.event(e,107)
