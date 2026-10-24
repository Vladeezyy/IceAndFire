"""Pixel width of the bestiary text lines with the bitmap glyphs of the Minecraft font.

The bestiary draws a page's text lines (trimmed, no '<...>' lines) at x 15 on the left page (lines 0-19) and at x 220
on the right page, the vanilla font scaled by 0.945 (GuiBestiary.writeFromTxt). A line wider than the page runs over
the binding or the edge of the book.

usage: python tools/bestiary_width.py <client resources jar> [--fix] [lang ...]
       --fix rewraps a paragraph that has a line over the page into the same number of lines (the pictures of a page
       are placed by coordinates, a line more would move the text under them); what does not fit is only reported
       (jar: build/moddev/artifacts/neoforge-*-client-extra-aka-minecraft-resources.jar)
"""
import glob
import io
import json
import os
import sys
import zipfile

from PIL import Image

sys.stdout.reconfigure(encoding='utf-8')
BESTIARY = 'src/main/resources/assets/iceandfire/lang/bestiary'
# Widest text that fits, in font pixels before the 0.945 scale. bestiary.png (512 px) is drawn 390 wide: the left
# parchment ends at 192.7 (the binding), the right one at 385.4; the text starts at 15 * 0.945 and 220 * 0.945.
LIMIT = {'left': 189, 'right': 187}


def glyph_widths(jar):
    z = zipfile.ZipFile(jar)
    widths = {' ': 4}
    providers = json.loads(z.read('assets/minecraft/font/include/default.json'))['providers']
    for provider in providers:
        if provider['type'] != 'bitmap':
            continue
        namespace, path = provider['file'].split(':')
        image = Image.open(io.BytesIO(z.read(f'assets/{namespace}/textures/{path}'))).convert('RGBA')
        rows = provider['chars']
        cell_w = image.width // len(rows[0])
        cell_h = image.height // len(rows)
        scale = provider.get('height', 8) / cell_h
        for r, row in enumerate(rows):
            for c, char in enumerate(row):
                if char in widths or char == '\u0000':
                    continue
                right = 0
                for x in range(cell_w):
                    for y in range(cell_h):
                        if image.getpixel((c * cell_w + x, r * cell_h + y))[3] > 0:
                            right = x + 1
                            break
                widths[char] = int(0.5 + right * scale) + 1
    return widths


def width(text, widths):
    total = 0
    skip = False
    for char in text:
        if skip:
            skip = False
            continue
        if char == '§':
            skip = True
            continue
        total += widths.get(char, 9)
    return total


def is_tag(text):
    return '<' in text or '>' in text


def rewrap(text, widths, limits):
    """The words of a paragraph in at most len(limits) lines (one limit per line), or None when they do not fit."""
    out = ['']
    for word in text.split():
        candidate = (out[-1] + ' ' + word).strip()
        if width(candidate, widths) <= limits[len(out) - 1]:
            out[-1] = candidate
        elif len(out) < len(limits) and width(word, widths) <= limits[len(out)]:
            out.append(word)
        else:
            return None
    return out


def fix_page(path, widths):
    """Rewraps the paragraphs with a line over the page; returns the lines it could not fit.

    Every line that is not a tag takes a place on the page, blank lines too (GuiBestiary.writeFromTxt): a paragraph
    may grow into the blank lines right after it, the lines below keep their places.
    """
    raw = open(path, 'rb').read().decode('utf-8')
    lines = raw.splitlines(keepends=True)
    content = [line.rstrip('\r\n') for line in lines]
    endings = [line[len(content[i]):] for i, line in enumerate(lines)]
    side = {}
    number = 0
    for i, text in enumerate(content):
        if not is_tag(text):
            side[i] = 'left' if number <= 19 else 'right'
            number += 1
    left = []
    i = 0
    while i < len(content):
        if i not in side or not content[i].strip():
            i += 1
            continue
        # A heading ('§n...') stands alone, a paragraph does not take the next heading either
        j = i
        while (j + 1 < len(content) and j + 1 in side and content[j + 1].strip()
               and not content[i].strip().startswith('§') and not content[j + 1].strip().startswith('§')):
            j += 1
        spare = j
        while spare + 1 < len(content) and spare + 1 in side and not content[spare + 1].strip():
            spare += 1
        block = list(range(i, j + 1))
        if any(width(content[k].strip(), widths) > LIMIT[side[k]] for k in block):
            room = list(range(i, spare + 1))
            new = rewrap(' '.join(content[k] for k in block), widths, [LIMIT[side[k]] for k in room])
            if new is None:
                left += [content[k].strip() for k in block if width(content[k].strip(), widths) > LIMIT[side[k]]]
            else:
                new += [''] * (len(room) - len(new))
                indent = content[i][:len(content[i]) - len(content[i].lstrip())]
                for n, k in enumerate(room):
                    content[k] = (indent if n == 0 else '') + new[n]
        i = spare + 1
    open(path, 'wb').write(''.join(c + e for c, e in zip(content, endings)).encode('utf-8'))
    return left


def main():
    widths = glyph_widths(sys.argv[1])
    fix = '--fix' in sys.argv
    args = [a for a in sys.argv[2:] if a != '--fix']
    langs = args or sorted(os.path.basename(d)[:-2] for d in glob.glob(f'{BESTIARY}/*_0'))
    if fix:
        for lang in langs:
            for path in sorted(glob.glob(f'{BESTIARY}/{lang}_0/*.txt')):
                if not path.endswith('example.txt'):
                    for line in fix_page(path, widths):
                        print(f'{lang} {os.path.basename(path)} does not fit: {line}')
    over = 0
    for lang in langs:
        for path in sorted(glob.glob(f'{BESTIARY}/{lang}_0/*.txt')):
            if path.endswith('example.txt'):
                continue
            number = 0
            for raw in open(path, encoding='utf-8'):
                line = raw.strip()
                if '<' in line or '>' in line:
                    continue
                side = 'left' if number <= 19 else 'right'
                number += 1
                w = width(line, widths)
                if w > LIMIT[side]:
                    over += 1
                    print(f'{lang} {os.path.basename(path)} {side} {w}px: {line}')
    print('lines over the page:', over)


if __name__ == '__main__':
    main()
