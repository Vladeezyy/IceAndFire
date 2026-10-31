"""26.1.2 -> 1.21.11 on the lines javac reported (build-compile.log), where the fix depends on the receiver's type:
player.sendSystemMessage(c) -> displayClientMessage(c, false), entity.is(entity type tag) -> entity.getType().is(..),
the record accessors of ChunkPos -> its fields. Run after a compile."""
import collections
import re

log = open('build-compile.log').read().split('\n')
todo = collections.defaultdict(set)
for i, line in enumerate(log):
    m = re.match(r'^(/\S+\.java):(\d+): error: (.*)', line)
    if not m:
        continue
    msg = m.group(3) + ' ' + ' '.join(log[i + 1:i + 5])
    if 'sendSystemMessage(MutableComponent)' in msg or 'sendSystemMessage(Component)' in msg:
        todo[m.group(1)].add((int(m.group(2)), 'msg'))
    elif 'cannot be converted to Entity' in msg or 'conforms to Entity' in msg:
        todo[m.group(1)].add((int(m.group(2)), 'tag'))
    elif 'symbol:   method x()' in msg or 'symbol:   method z()' in msg:
        todo[m.group(1)].add((int(m.group(2)), 'chunk'))


def args_end(s, i):
    depth = 1
    while depth:
        depth += {'(': 1, ')': -1}.get(s[i], 0)
        i += 1
    return i


for path, items in todo.items():
    lines = open(path).read().split('\n')
    for n, kind in sorted(items):
        s = lines[n - 1]
        if kind == 'msg':
            out, pos = '', 0
            for m in re.finditer(r'\.sendSystemMessage\(', s):
                end = args_end(s, m.end())
                out += s[pos:m.start()] + '.displayClientMessage(' + s[m.end():end - 1] + ', false)'
                pos = end
            s = out + s[pos:]
        elif kind == 'tag':
            s = re.sub(r'(\w+)\.is\((?=\(*(?:EntityTypeTags|IafEntityTags|TagKey\.create\(Registries\.ENTITY_TYPE))', r'\1.getType().is(', s)
        else:
            s = re.sub(r'\.([xz])\(\)', r'.\1', s)
        lines[n - 1] = s
    open(path, 'w').write('\n'.join(lines))
    print(path.split('/')[-1], sorted(items))
