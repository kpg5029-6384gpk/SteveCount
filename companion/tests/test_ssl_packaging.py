"""Regression for the distribution runtime's extra SSL certificate asset."""
import pathlib
import shutil
import ssl
import subprocess
import sys
import pytest

def test_relocated_ssl_keeps_verification(tmp_path):
    source = pathlib.Path(ssl.__file__)
    asset = source.with_name('codex-rejected-cas.pem')
    if not asset.is_file():
        pytest.skip('Standard Python does not require this distribution-specific asset')
    shutil.copy2(source, tmp_path / 'ssl.py')
    code = (
        'import sys; sys.path.insert(0, sys.argv[1]); import ssl; '
        'ctx=ssl.create_default_context(); '
        'assert ctx.check_hostname; assert ctx.verify_mode == ssl.CERT_REQUIRED; '
        'print("Verified TLS context created")'
    )
    missing = subprocess.run([sys.executable, '-c', code, str(tmp_path)], capture_output=True, text=True)
    assert missing.returncode != 0 and 'FileNotFoundError' in missing.stderr
    shutil.copy2(asset, tmp_path / asset.name)
    fixed = subprocess.run([sys.executable, '-c', code, str(tmp_path)], capture_output=True, text=True)
    assert fixed.returncode == 0, fixed.stderr
    assert 'Verified TLS context created' in fixed.stdout
