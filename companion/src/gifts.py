"""Strict, bounded, non-replaying Hand Heart trigger feed (no sender data exposed)."""
import time
import uuid
from collections import deque, OrderedDict


class Gifts:
    def __init__(self):
        self.session = str(uuid.uuid4())
        self.sequence = 0
        self.events = deque(maxlen=32)
        self.seen = OrderedDict()
        self.last = None

    def event(self, event, now=None):
        now = time.time() if now is None else now
        gift = getattr(event, 'gift', None)
        if gift is None or event.streaking:
            return False
        # Fail closed if upstream omits metadata or changes the name/value.
        if str(gift.name).strip().casefold() not in ('hand heart', 'hand hearts') or gift.diamond_count != 100:
            return False
        if not isinstance(event.gift_id, int) or event.gift_id <= 0:
            return False
        group = getattr(event, 'group_id', 0)
        user = getattr(getattr(event, 'user', None), 'id', 0)
        message = getattr(getattr(event, 'common', None), 'msg_id', 0)
        key = ('group', str(user), str(group), event.gift_id) if group and user else ('message', str(message)) if message else None
        # Never damage on unidentifiable events. Streak totals never multiply damage.
        if key is None or key in self.seen:
            return False
        self.seen[key] = now
        while len(self.seen) > 2048:
            self.seen.popitem(last=False)
        # One accepted gift per five seconds globally. Dropped gifts are not queued.
        if self.last is not None and now - self.last < 5:
            return False
        self.last = now
        self.sequence += 1
        self.events.append(dict(sequence=self.sequence, created=now))
        return True

    def snapshot(self, now=None):
        now = time.time() if now is None else now
        return dict(session=self.session, sequence=self.sequence,
                    events=[e for e in self.events if 0 <= now - e['created'] <= 5])
