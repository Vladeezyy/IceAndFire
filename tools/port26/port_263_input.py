#!/usr/bin/env python3
"""26.2 -> 26.3: GLFW is gone (SDL input). Rewrites the Java sources of a repo (common/src, neoforge/src, fabric/src).
Usage: port_263_input.py <repo root>   Running it again changes nothing.

- GLFW.GLFW_KEY_UNKNOWN -> InputConstants.UNKNOWN.getValue() (0; there is no KEY_UNKNOWN);
- GLFW.GLFW_KEY_X -> InputConstants.KEY_X (SDL scancodes: KEY_I = 12, KEY_ESCAPE = 41; the names differ for some
  keys, e.g. GLFW_KEY_LEFT_SHIFT -> KEY_LSHIFT, GLFW_KEY_ENTER -> KEY_RETURN), GLFW_PRESS/RELEASE/REPEAT ->
  InputConstants.PRESS/RELEASE/REPEAT, GLFW_MOUSE_BUTTON_LEFT/RIGHT/MIDDLE -> InputConstants.MOUSE_BUTTON_*;
- GLFW.glfwSetWindowSize(window.handle(), w, h) -> window.setWindowed(w, h);
- new MouseButtonInfo(0, m) / (1, m) / (2, m) -> MOUSE_BUTTON_LEFT / RIGHT / MIDDLE: SDL's left button is 1, so a
  scene clicking with button 0 clicks nothing (no error);
- Entity.setInvulnerable -> setPermanentlyInvulnerable; GuiGraphicsExtractor.tooltip(..., positioner, null) -> (..., null, false);
- the GLFW import goes, the InputConstants import comes where needed.
Check what is left with: grep -rn 'GLFW\\|lwjgl.glfw' */src
"""
import pathlib
import re
import sys

KEYS = {'LEFT_SHIFT': 'LSHIFT', 'RIGHT_SHIFT': 'RSHIFT', 'LEFT_CONTROL': 'LCONTROL', 'RIGHT_CONTROL': 'RCONTROL',
        'LEFT_ALT': 'LALT', 'RIGHT_ALT': 'RALT', 'ENTER': 'RETURN'}
IC = 'com.mojang.blaze3d.platform.InputConstants'
RULES = [
    # no key: SDL has no KEY_UNKNOWN constant (GLFW's -1), the unknown key is 0
    (re.compile(r'\bGLFW\.GLFW_KEY_UNKNOWN\b'), lambda m: 'InputConstants.UNKNOWN.getValue()'),
    (re.compile(r'\bGLFW\.GLFW_KEY_(\w+)\b'), lambda m: f'InputConstants.KEY_{KEYS.get(m[1], m[1])}'),
    (re.compile(r'\bGLFW\.GLFW_(PRESS|RELEASE|REPEAT)\b'), lambda m: f'InputConstants.{m[1]}'),
    (re.compile(r'\bGLFW\.GLFW_MOUSE_BUTTON_(LEFT|RIGHT|MIDDLE)\b'), lambda m: f'InputConstants.MOUSE_BUTTON_{m[1]}'),
    (re.compile(r'(?:\borg\.lwjgl\.glfw\.)?\bGLFW\.glfwSetWindowSize\(([\w.()]+?)\.handle\(\), '), lambda m: f'{m[1]}.setWindowed('),
    (re.compile(r'\bMouseButtonInfo\(([012]), '), lambda m: f'MouseButtonInfo({IC}.MOUSE_BUTTON_' + {'0': 'LEFT', '1': 'RIGHT', '2': 'MIDDLE'}[m[1]] + ', '),
    (re.compile(r'\.setInvulnerable\('), lambda m: '.setPermanentlyInvulnerable('),
    # GuiGraphicsExtractor.tooltip(font, lines, x, y, positioner, style) got a last boolean (extra space after the first line)
    (re.compile(r'(DefaultTooltipPositioner\.INSTANCE, null)\);'), lambda m: m[1] + ', false);'),
]

root = pathlib.Path(sys.argv[1])
# multiloader (common/neoforge/fabric) or a single-loader project (src)
for base in ['common/src', 'neoforge/src', 'fabric/src', 'src']:
    for path in (root / base).rglob('*.java') if (root / base).exists() else []:
        text = path.read_text(encoding='utf-8')
        new = text
        for pattern, repl in RULES:
            new = pattern.sub(repl, new)
        if 'GLFW.' not in new:
            new = re.sub(r'import org\.lwjgl\.glfw\.GLFW;\n', '', new)
        if re.search(r'(?<![.\w])InputConstants\.', new) and f'import {IC};' not in new:
            new = re.sub(r'^(package [\w.]+;\n)', r'\1\nimport ' + IC + ';\n', new, count=1, flags=re.M)
        if new != text:
            path.write_text(new, encoding='utf-8')
            print('ported', path.relative_to(root))
