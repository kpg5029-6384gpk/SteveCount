import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).parents[1]/'src'))
import pytest
from voice import Voice, parse_command, PHRASES

@pytest.mark.parametrize('phrase,expected', list(PHRASES.items()))
def test_commands(phrase, expected):
    assert parse_command(phrase)==expected
    assert parse_command(phrase.upper()+'!')==expected

@pytest.mark.parametrize('phrase', ['dance','buddy dance','little buddy','little buddy punch',
    'little buddy kill me', 'do not dance little buddy', 'little buddy dance and wave', None, 12, 'x'*300])
def test_unrelated_or_ambiguous_speech(phrase):
    assert parse_command(phrase) is None

def test_feed_cooldown_mute_and_bounded_history():
    now=[100.]
    v=Voice(lambda:now[0])
    assert v.snapshot()['status']=='Off'
    assert v.recognized('dance little buddy')
    assert not v.recognized('dance little buddy')
    now[0]+=2
    assert v.recognized('little buddy hop off')
    assert v.snapshot()['events'][-1]['command']=='head_off'
    for i in range(30):
        now[0]+=2
        v.recognized('little buddy wave')
    assert len(v.snapshot()['events'])==16
    assert all(set(e)=={'sequence','created','command'} for e in v.snapshot()['events'])
    v.stop()
    assert not v.recognized('little buddy dance')
    assert v.snapshot()['events']==[]

def test_missing_microphone_does_not_crash(monkeypatch):
    import sounddevice
    monkeypatch.setattr(sounddevice,'query_devices',lambda *a: (_ for _ in ()).throw(RuntimeError('No microphone')))
    v=Voice()
    v._listen(None)
    assert 'Voice unavailable' in v.snapshot()['status']
