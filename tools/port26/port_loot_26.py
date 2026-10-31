"""1.21.2+: Entity.getDefaultLootTable() returns Optional<ResourceKey<LootTable>> (empty: no loot)."""
import pathlib
import re


def body_end(text, start):
    depth, i = 1, start
    while depth:
        depth += {'{': 1, '}': -1}.get(text[i], 0)
        i += 1
    return i


def wrap(m):
    value = m.group(1).strip()
    if value == 'BuiltInLootTables.EMPTY' or value == 'null':
        return 'return Optional.empty();'
    if 'getDefaultLootTable()' in value and value.startswith(('super.', 'this.')):
        return m.group(0)
    return 'return Optional.of(' + value + ');'


changed = 0
for path in pathlib.Path('common/src/main/java').rglob('*.java'):
    text = path.read_text(encoding='utf-8')
    new, pos, out = text, 0, ''
    for m in re.finditer(r'(?<=\s)ResourceKey<LootTable> getDefaultLootTable\(\)(\s*)\{', text):
        end = body_end(text, m.end())
        body = re.sub(r'return ([^;]+);', wrap, text[m.end():end])
        out += text[pos:m.start()] + 'Optional<ResourceKey<LootTable>> getDefaultLootTable()' + m.group(1) + '{' + body
        pos = end
    new = out + text[pos:]
    if new != text:
        if 'import java.util.Optional;' not in new:
            new = re.sub(r'^(package [\w.]+;\n)', r'\1import java.util.Optional;\n', new, count=1, flags=re.M)
        path.write_text(new, encoding='utf-8')
        changed += 1
print(changed, 'files changed')
