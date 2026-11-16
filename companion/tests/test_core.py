import sys
from pathlib import Path
from types import SimpleNamespace
sys.path.insert(0, str(Path(__file__).parents[1] / 'src'))
import pytest
from core import State, normalize, count, backoff

@pytest.mark.parametrize('value', ['@Example', 'example', 'https://www.tiktok.com/@Example/live?x=1', 'https://tiktok.com/@Example'])
def test_normalize(value):
    assert normalize(value) == 'example'

@pytest.mark.parametrize('value', ['https://evil.com/@x/live', 'https://tiktok.com.evil.com/@x', 'http://tiktok.com/@x', 'https://www.tiktok.com/@x/video/1', '', 'a/b', 'https://user@tiktok.com/@x', 'https://tiktok.com:123/@x'])
def test_bad_input(value):
    with pytest.raises(ValueError): normalize(value)

@pytest.mark.parametrize('value,expected', [(0,0), ('1',1), ('128',128), ('1,250',1250), ('12.5K',12500), ('1.2M',1200000)])
def test_counts(value, expected):
    assert count(value) == expected

@pytest.mark.parametrize('value', [-1, None, True, 1.5, 'NaN', '1,25', 'unknown'])
def test_malformed(value):
    with pytest.raises(ValueError): count(value)

def test_state():
    s = State()
    assert s.snapshot()['viewers'] is None
    s.event(SimpleNamespace(total=0, total_user=99999), now=100)
    assert s.viewers == 0 and not s.snapshot(now=101)['stale']
    s.event(SimpleNamespace(total=128), now=110)
    s.event(SimpleNamespace(total=3), now=120)
    assert s.peak == 128
    assert s.snapshot(now=181)['stale']
    s.status = 'Reconnecting'
    assert s.snapshot(now=121)['stale'] and s.viewers == 3
    with pytest.raises(ValueError): s.event(SimpleNamespace(total=-1))
    assert s.viewers == 3

def test_backoff():
    assert [backoff(n) for n in range(9)] == [1,2,4,8,16,32,64,120,120]

@pytest.mark.parametrize('value', ['www.tiktok.com/@Example/live', 'tiktok.com/@Example', 'm.tiktok.com/@Example/live?x=1'])
def test_schemeless_creator_links(value):
    assert normalize(value) == 'example'

@pytest.mark.parametrize('value', [None, 123, {}, [], 'Fishy Name', 'www.tiktok.com.evil.com/@x'])
def test_invalid_username_types(value):
    with pytest.raises(ValueError): normalize(value)

@pytest.mark.parametrize('value', ['.fishyplays', '@.fishyplays', 'https://www.tiktok.com/@.fishyplays/live'])
def test_leading_dot_handle(value):
    assert normalize(value) == '.fishyplays'
