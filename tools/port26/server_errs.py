"""Server-side (non-client) javac errors of build-compile.log, grouped, with one example each: python3 tools/port26/server_errs.py [n] [client]"""
import collections
import re
import sys

log = open('build-compile.log').read().split('* What went wrong')[0]
errs = re.findall(r'(/[^\n]+?\.java):(\d+): error: ([^\n]+)\n([^\n]*)\n[^\n]*\n(?:\s+symbol:\s+([^\n]+))?', log)
client = len(sys.argv) > 2
c, ex = collections.Counter(), {}
for f, l, m, code, s in errs:
    p = f.split('/iceandfire/')[-1]
    if (p.startswith('client') or 'compat/jei' in p) == client:
        k = (m + (' | ' + s.strip() if s else ''))[:100]
        c[k] += 1
        ex.setdefault(k, p.split('/')[-1] + ':' + l + ': ' + code.strip()[:130])
print(sum(c.values()), 'client' if client else 'server-side')
for k, v in c.most_common(int(sys.argv[1]) if len(sys.argv) > 1 else 60):
    print(v, k, '##', ex[k])
