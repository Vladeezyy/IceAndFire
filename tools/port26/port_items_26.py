"""1.21.2 - 26.1 item API (run from the repo root; idempotent):
- releaseUsing returns whether the item was used (the stat): void bodies return true at the end, false on early exits;
- getDescriptionId() is final: the overrides become getName(stack) with the same translation key;
- cooldowns are per stack: addCooldown/isOnCooldown/getCooldownPercent(this or stack.getItem()) take the stack.
"""
import pathlib
import re


def body_end(text, i):
    depth = 1
    while depth:
        depth += {'{': 1, '}': -1}.get(text[i], 0)
        i += 1
    return i


changed = 0
for path in pathlib.Path('common/src/main/java').rglob('*.java'):
    text = path.read_text(encoding='utf-8')
    new = text
    # releaseUsing
    out, pos = '', 0
    for m in re.finditer(r'public void releaseUsing\(((?:@\w+ )?(?:final )?ItemStack (\w+)[^)]*)\)\s*\{', new):
        end = body_end(new, m.end())
        body = new[m.end():end - 1]
        body = re.sub(r'\breturn;', 'return false;', body)
        out += new[pos:m.start()] + 'public boolean releaseUsing(' + m.group(1) + ') {' + body.rstrip() + '\n        return true;\n    }'
        pos = end
    new = out + new[pos:]
    # getDescriptionId() -> getName(stack)
    new = re.sub(r'public (?:@NotNull )?String getDescriptionId\(\) \{',
                 'public @NotNull Component getName(@NotNull ItemStack iafStack) {\n        return Component.translatable(this.iafDescriptionId());\n    }\n\n    private String iafDescriptionId() {', new)
    m = re.search(r'public (?:@NotNull )?String getDescriptionId\(((?:@\w+ )?ItemStack (\w+))\) \{', new)
    if m:
        end = body_end(new, m.end())
        body = new[m.end():end - 1].replace('super.getDescriptionId(' + m.group(2) + ')', 'this.getDescriptionId()')
        new = new[:m.start()] + 'public @NotNull Component getName(' + m.group(1) + ') {\n        return Component.translatable(this.iafDescriptionId(' + m.group(2) + '));\n    }\n\n    private String iafDescriptionId(ItemStack ' + m.group(2) + ') {' + body + new[end - 1:]
    # cooldowns
    for method in ('addCooldown', 'isOnCooldown', 'getCooldownPercent'):
        def per_stack(mm):
            # the stack of this item in the method: an ItemStack parameter or the held stack local
            before = new[:mm.start()]
            decl = list(re.finditer(r'ItemStack (\w+)(?: = |[,)])', before[-3000:]))
            name = decl[-1].group(1) if decl else 'stack'
            return '.' + method + '(' + name
        new = re.sub(r'\.' + method + r'\((?:this|\w+\.getItem\(\))(?=[,)])', per_stack, new)
    if new != text:
        if 'Component' in new and 'import net.minecraft.network.chat.Component;' not in new:
            new = re.sub(r'^(package [\w.]+;\n)', r'\1import net.minecraft.network.chat.Component;\n', new, count=1, flags=re.M)
        path.write_text(new, encoding='utf-8')
        changed += 1
print(changed, 'files changed')
