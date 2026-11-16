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

def test_missing_microphone_does_not_crash(monkeypatch, tmp_path):
    import sounddevice
    import voice
    import vosk
    (tmp_path/'am').mkdir()
    (tmp_path/'am/final.mdl').touch()
    monkeypatch.setattr(voice,'model_path',lambda:tmp_path)
    monkeypatch.setattr(vosk,'Model',lambda *args:object())
    monkeypatch.setattr(sounddevice,'query_devices',lambda *a: (_ for _ in ()).throw(RuntimeError('No microphone')))
    v=Voice()
    v._listen(None)
    assert 'No microphone' in v.snapshot()['status']
    # Muting while the model loads must not subsequently open the microphone.
    import vosk
    muted=Voice()
    def loading_model(*args):
        muted.stop()
        return object()
    monkeypatch.setattr(vosk,'Model',loading_model)
    muted._listen(None)
    assert muted.snapshot()['status']=='Off'


def test_feeling_phrase_and_action():
    assert parse_command("How you feeling little buddy?")=="Feeling"
    assert parse_command("Little buddy, how are you feeling?")=="Feeling"
    assert parse_command("how do you feel little buddy")=="Feeling"
    assert parse_command("how you feeling") is None
    v=Voice(lambda:100.)
    assert v.recognized("How you feeling little buddy?")
    assert v.snapshot()['events'][-1]['command']=="Feeling"

@pytest.mark.parametrize('text,command', [
    ('Little buddy, can you dance please?', 'Dance'),
    ('Could you wave little buddy please?', 'Wave'),
    ('Little buddy please hop on my head', 'head_on'),
])
def test_natural_requests(text, command):
    assert parse_command(text) == command

def test_pause_after_wake_phrase():
    now = [100.]
    voice = Voice(lambda: now[0])
    assert not voice.recognized('dance')
    assert not voice.recognized('little buddy')
    now[0] += 1
    assert not voice.recognized('')
    now[0] += 1
    assert voice.recognized('can you dance please')
    now[0] += 2
    assert not voice.recognized('wave')
    assert not voice.recognized('little buddy')
    now[0] += 4
    assert not voice.recognized('wave')
    assert not voice.recognized('little buddy')
    assert not voice.recognized('do not dance')
    assert not voice.recognized('dance')
    assert not voice.recognized('little buddy')
    voice.stop()
    assert not voice.recognized('dance')

@pytest.mark.parametrize('text', ['little buddy do not dance', 'little buddy can you dance and wave',
    'please do not dance little buddy', 'can you punch little buddy', 'little buddy please kill me'])
def test_polite_requests_still_require_safe_unambiguous_actions(text):
    assert parse_command(text) is None
