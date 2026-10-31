"""1.21.2+: anonymous Predicate<X> target selectors of NearestAttackableTargetGoal (and subclasses) -> TargetingConditions.Selector.

    python3 tools/port26/port_selectors.py      (repo root, after a compile: build-compile.log)

The error lines name the goal; its anonymous predicate becomes a Selector whose test(target, level) casts the target
to the predicate's type as before. Predicates passed on to a goal are wrapped in IafSelectors.of.
"""
import re

log = open('build-compile.log').read().split('* What went wrong')[0]
errs = re.findall(r'(/[^\n]+?\.java):(\d+): error: ([^\n]+)', log)
by_file = {}
for f, l, m in errs:
    if 'cannot be converted to Selector' in m or 'cannot infer type arguments for NearestAttackableTargetGoal' in m:
        by_file.setdefault(f, set()).add(int(l))


def body_end(text, i):
    depth = 1
    while depth:
        depth += {'{': 1, '}': -1}.get(text[i], 0)
        i += 1
    return i


for f, lines in by_file.items():
    text = open(f).read().split('\n')
    changed = False
    for ln in sorted(lines, reverse=True):
        line = text[ln - 1]
        m = re.search(r'new Predicate<([\w.]+)>\(\)\s*\{', line)
        if m:
            kind = m.group(1)
            rest = '\n'.join(text[ln - 1:])
            start = rest.index(m.group(0))
            end = body_end(rest, start + len(m.group(0)))
            anon = rest[start:end]
            tm = re.search(r'public boolean (?:test|apply)\((?:@\w+ )*(?:final )?(?:[\w.]+) (\w+)\)\s*\{', anon)
            if not tm:
                print('no test()', f, ln)
                continue
            name = tm.group(1)
            if kind == 'LivingEntity':
                header = 'public boolean test(LivingEntity ' + name + ', ServerLevel serverLevel) {'
            else:
                header = 'public boolean test(LivingEntity iafTarget, ServerLevel serverLevel) {\n                ' + kind + ' ' + name + ' = (' + kind + ') iafTarget;' if kind != 'Entity' else 'public boolean test(LivingEntity iafTarget, ServerLevel serverLevel) {\n                Entity ' + name + ' = iafTarget;'
            new_anon = anon.replace(m.group(0), 'new TargetingConditions.Selector() {', 1)
            new_anon = new_anon.replace(tm.group(0), header, 1)
            rest = rest[:start] + new_anon + rest[end:]
            text = text[:ln - 1] + rest.split('\n')
            changed = True
        else:
            new = re.sub(r', (\w+)\);\s*$', r', IafSelectors.of(\1));', line)
            new = re.sub(r'\.selector\((\w+)\)', r'.selector(IafSelectors.of(\1))', new)
            if new != line:
                text[ln - 1] = new
                changed = True
            else:
                print('left', f.split('/')[-1], ln, line.strip()[:120])
    if changed:
        out = '\n'.join(text)
        out = out.replace('new NearestAttackableTargetGoal<>(', 'new NearestAttackableTargetGoal<LivingEntity>(') if False else out
        for imp in ['net.minecraft.world.entity.ai.targeting.TargetingConditions', 'net.minecraft.server.level.ServerLevel', 'com.github.alexthe666.iceandfire.entity.util.IafSelectors']:
            simple = imp.rsplit('.', 1)[1]
            if simple in out and 'import ' + imp + ';' not in out and not out.startswith('package ' + imp.rsplit('.', 1)[0] + ';'):
                out = re.sub(r'^(package [\w.]+;\n)', r'\1import ' + imp + ';\n', out, count=1, flags=re.M)
        open(f, 'w').write(out)
        print('changed', f.split('/')[-1])
