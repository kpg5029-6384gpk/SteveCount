"""Provider independent state; unknown counts remain None, never synthetic zero."""
import re
import time
from dataclasses import dataclass, asdict
from urllib.parse import urlsplit

def normalize(value):
    if not isinstance(value, str):
        raise ValueError("Enter a TikTok @username or creator LIVE URL")
    value = value.strip()
    if not value:
        raise ValueError("Enter a TikTok @username before connecting")
    if value.lower().startswith(("tiktok.com/", "www.tiktok.com/", "m.tiktok.com/")):
        value = "https://" + value
    if '://' in value:
        url = urlsplit(value)
        if url.scheme != 'https' or url.hostname not in ('tiktok.com', 'www.tiktok.com', 'm.tiktok.com') or url.port or url.username:
            raise ValueError('Use a TikTok username or https://www.tiktok.com/@username/live')
        match = re.fullmatch(r'/@([A-Za-z0-9_.]{1,24})(?:/live)?/?', url.path)
        if not match:
            raise ValueError('Use a creator profile or LIVE URL; shortened links are unsupported')
        value = match[1]
    value = value.removeprefix('@')
    if not re.fullmatch(r'[A-Za-z0-9_.]{1,24}', value):
        raise ValueError('Use the TikTok @username, not the display name (letters, numbers, dots and underscores only)')
    return value.lower()

def count(value):
    if isinstance(value, bool):
        raise ValueError('Invalid count')
    if isinstance(value, int) and 0 <= value <= 2**53:
        return value
    if isinstance(value, str) and re.fullmatch(r'(?:\d+|\d{1,3}(?:,\d{3})+)(?:\.\d+)?[KM]?', value.upper()):
        text = value.upper().replace(',', '')
        factor = {'K': 1000, 'M': 1000000}.get(text[-1], 1)
        n = float(text[:-1] if factor != 1 else text) * factor
        if n.is_integer() and 0 <= n <= 2**53:
            return int(n)
    raise ValueError('Malformed viewer count')

@dataclass
class State:
    status: str = 'Not Configured'
    username: str = ''
    viewers: int | None = None
    peak: int = 0
    updated: float = 0
    message: str = ''
    room: str = ''

    def event(self, event, now=None):
        # TikTokLive 7.0.1 RoomUserSeqEvent.total is concurrent viewers.
        # total_user is cumulative and must never be used here.
        self.viewers = count(event.total)
        self.peak = max(self.peak, self.viewers)
        self.updated = time.time() if now is None else now
        self.status = 'Live'
        self.message = ''

    def snapshot(self, now=None):
        result = asdict(self)
        result['stale'] = self.viewers is not None and (self.status != 'Live' or (time.time() if now is None else now) - self.updated > 60)
        return result

def backoff(attempt):
    return min(120, 2 ** min(attempt, 7))
