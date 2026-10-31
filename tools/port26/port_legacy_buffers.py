"""Converted submit(...) methods (port_renderers.py) that still draw by hand through a MultiBufferSource: the buffer
becomes an IafBufferSource (records the vertices, submits them at the end of the method). Idempotent."""
import pathlib
import re

ROOT = pathlib.Path('common/src/main/java/com/github/alexthe666/iceandfire/client')


def body_end(text, i):
    depth = 1
    while depth:
        depth += {'{': 1, '}': -1}.get(text[i], 0)
        i += 1
    return i


for path in ROOT.rglob('*.java'):
    text = path.read_text()
    out, pos, changed = '', 0, False
    for m in re.finditer(r'(public|protected) void submit\([^)]*SubmitNodeCollector (\w+)[^)]*\)\s*\{', text):
        end = body_end(text, m.end())
        body = text[m.end():end - 1]
        names = set(re.findall(r'\b(\w+)\.getBuffer\(', body)) | set(re.findall(r'[(,]\s*(bufferIn|buffer|bufferSource|buffers|pBuffer|bufferSourceIn|iRenderTypeBuffer)\s*[,)]', body))
        names = {n for n in names if not re.search(r'(?:MultiBufferSource|IafBufferSource|VertexConsumer) ' + n + r'\b', body) and n not in ('this',)}
        if names and 'new IafBufferSource()' not in body:
            name = sorted(names)[0]
            body = '\n        IafBufferSource ' + name + ' = new IafBufferSource();' + body.rstrip() + '\n        ' + name + '.submit(' + m.group(2) + ');\n    '
            # early returns submit what was drawn so far
            body = re.sub(r'(\n\s*)return;', r'\1' + name + '.submit(' + m.group(2) + r');\1return;', body)
            changed = True
        out += text[pos:m.end()] + body + '}'
        pos = end
    out += text[pos:]
    if changed:
        if 'import com.github.alexthe666.iceandfire.client.render.IafBufferSource;' not in out and 'package com.github.alexthe666.iceandfire.client.render;' not in out:
            out = re.sub(r'^(package [\w.]+;\n)', r'\1import com.github.alexthe666.iceandfire.client.render.IafBufferSource;\n', out, count=1, flags=re.M)
        path.write_text(out)
        print('changed', path.name)
