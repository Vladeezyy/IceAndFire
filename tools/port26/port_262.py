"""26.1.2 -> 26.2: mechanical rewrites of the Java sources (run from the repo root; running it again changes nothing).
Generic version of Matter Overdrive's tools/port/port_262.py.

- the screen and the HUD live in Minecraft.gui: mc.screen -> mc.gui.screen(), mc.setScreen(x) -> mc.gui.setScreen(x),
  options.hideGui -> gui.hud: reads -> gui.hud.isHidden(); assignments -> setHudHidden(mc, b), a private static helper
  added to the file (Hud only has toggle());
- entity type constants in EntityTypes; BlockPos.getCenter / getBottomCenter -> Vec3.atCenterOf / atBottomCenterOf;
- coloured and copper block collections (Items.BANNER.red(), Items.CONCRETE.white(), Items.CHISELED_COPPER.waxed().oxidized());
- overlay and toasts in Minecraft.gui; GameRenderer accessors (mainCamera, mc.getMainRenderTarget() -> mc.gameRenderer.mainRenderTarget(), renderBuffers, featureRenderDispatcher), LightCoordsUtil,
  Language.has (also for I18n.exists), the uniform / alt font ids, team colours, ChatFormatting colours as TextColor,
  MobEffect#isInstantaneous / applyInstantaneousEffect.
"""
import pathlib
import re

MC = r'(\b(?:mc|minecraft|this\.minecraft|Minecraft\.getInstance\(\)|net\.minecraft\.client\.Minecraft\.getInstance\(\)))'
RENAMES = [
    (MC + r'\.screen\b(?!\()', r'\1.gui.screen()'),
    (MC + r'\.setScreen\(', r'\1.gui.setScreen('),
    (MC + r'\.options\.hideGui = (true|false)', r'setHudHidden(\1, \2)'),
    (MC + r'\.options\.hideGui\b', r'\1.gui.hud.isHidden()'),
    (r'\bnet\.minecraft\.world\.entity\.EntityType\.([A-Z][A-Z0-9_]+)\b', r'net.minecraft.world.entity.EntityTypes.\1'),
    (r'(?<![.\w])EntityType\.([A-Z][A-Z0-9_]+)\b', r'EntityTypes.\1'),
    (r'(?<![\w.])((?![\w.]*(?:[Hh]ive|[Vv]illage)\w*(?:\(\))?\.getCenter)[\w.]+(?:\([^()]*\))?)\.getCenter\(\)', r'net.minecraft.world.phys.Vec3.atCenterOf(\1)'),  # not MyrmexHive.getCenter() (the mod's BlockPos)
    (r'([\w.]+(?:\([^()]*\))?)\.getBottomCenter\(\)', r'net.minecraft.world.phys.Vec3.atBottomCenterOf(\1)'),
    (r'\b(Items|Blocks)\.WAXED_OXIDIZED_CHISELED_COPPER\b', r'\1.CHISELED_COPPER.waxed().oxidized()'),
    (r'\b(Items|Blocks)\.OXIDIZED_CHISELED_COPPER\b', r'\1.CHISELED_COPPER.weathering().oxidized()'),
    (r'\b(Items|Blocks)\.CHISELED_COPPER\b(?!\.)', r'\1.CHISELED_COPPER.weathering().unaffected()'),
    (r'\.getMainCamera\(\)', '.mainCamera()'),
    (MC + r'\.getMainRenderTarget\(\)', r'\1.gameRenderer.mainRenderTarget()'),
    (MC + r'\.getOverlay\(\)', r'\1.gui.overlay()'),
    (MC + r'\.setOverlay\(', r'\1.gui.setOverlay('),
    (MC + r'\.getToastManager\(\)', r'\1.gui.toastManager()'),
    (r'\.getMainRenderTarget\(\)', '.mainRenderTarget()'),
    (r'\bmc\.renderBuffers\(\)', 'mc.gameRenderer.renderBuffers()'),
    (r'\.getFeatureRenderDispatcher\(\)', '.featureRenderDispatcher()'),
    (r'net\.minecraft\.client\.renderer\.LevelRenderer\.getLightCoords\(', 'net.minecraft.util.LightCoordsUtil.getLightCoords('),
    (r'\bLevelRenderer\.getLightCoords\(', 'net.minecraft.util.LightCoordsUtil.getLightCoords('),
    (r'net\.minecraft\.client\.resources\.language\.I18n\.exists\(', 'net.minecraft.locale.Language.getInstance().has('),
    (r'\bMinecraft\.UNIFORM_FONT\b', 'net.minecraft.resources.Identifier.withDefaultNamespace("uniform")'),
    (r'\bMinecraft\.ALT_FONT\b', 'net.minecraft.resources.Identifier.withDefaultNamespace("alt")'),
    (r'\.setColor\(net\.minecraft\.ChatFormatting\.(\w+)\)', r'.setColor(java.util.Optional.of(net.minecraft.world.scores.TeamColor.\1))'),
    (r'\.markPosForPostprocessing\(', '.markPosForPostProcessing('),
    (r'(?<![\w.])I18n\.exists\(', 'net.minecraft.locale.Language.getInstance().has('),
    (r'\.isInstantenous\(\)', '.isInstantaneous()'),
    (r'\.applyInstantenousEffect\(', '.applyInstantaneousEffect('),
    (r'\bChatFormatting\.([A-Z_]+)\.getColor\(\)', r'net.minecraft.network.chat.TextColor.\1.getValue()'),
]

# Coloured block / item collections (ColorCollection): Items.RED_BANNER -> Items.BANNER.red()
COLOURS = {'WHITE': 'white', 'ORANGE': 'orange', 'MAGENTA': 'magenta', 'LIGHT_BLUE': 'lightBlue', 'YELLOW': 'yellow',
           'LIME': 'lime', 'PINK': 'pink', 'GRAY': 'gray', 'LIGHT_GRAY': 'lightGray', 'CYAN': 'cyan', 'PURPLE': 'purple',
           'BLUE': 'blue', 'BROWN': 'brown', 'GREEN': 'green', 'RED': 'red', 'BLACK': 'black'}
# longest suffix first (CONCRETE_POWDER before CONCRETE, STAINED_GLASS_PANE before STAINED_GLASS)
COLLECTIONS = {'WOOL': 'WOOL', 'TERRACOTTA': 'DYED_TERRACOTTA', 'CARPET': 'CARPET', 'STAINED_GLASS_PANE': 'STAINED_GLASS_PANE',
               'STAINED_GLASS': 'STAINED_GLASS', 'SHULKER_BOX': 'DYED_SHULKER_BOX', 'GLAZED_TERRACOTTA': 'GLAZED_TERRACOTTA',
               'CONCRETE_POWDER': 'CONCRETE_POWDER', 'CONCRETE': 'CONCRETE', 'HARNESS': 'HARNESS', 'BUNDLE': 'DYED_BUNDLE',
               'DYE': 'DYE', 'BED': 'BED', 'BANNER': 'BANNER', 'WALL_BANNER': 'WALL_BANNER', 'CANDLE': 'DYED_CANDLE',
               'CANDLE_CAKE': 'DYED_CANDLE_CAKE'}
COLOUR_RE = re.compile(r'\b(Items|Blocks)\.(' + '|'.join(sorted(COLOURS, key=len, reverse=True)) + r')_('
                       + '|'.join(sorted(COLLECTIONS, key=len, reverse=True)) + r')\b(?!\.)')
RENAMES.append((COLOUR_RE, lambda m: f'{m.group(1)}.{COLLECTIONS[m.group(3)]}.{COLOURS[m.group(2)]}()'))

changed = 0
# multiloader (common/neoforge/fabric) or a single-loader project (src)
for base in ['common/src/main/java', 'common/src/selftest/java', 'neoforge/src', 'fabric/src', 'src']:
    for path in pathlib.Path(base).rglob('*.java'):
        text = path.read_text(encoding='utf-8')
        new = text
        for pattern, repl in RENAMES:
            new = re.sub(pattern, repl, new)
        if re.search(r'(?<![.\w])EntityTypes\.', new) and 'import net.minecraft.world.entity.EntityTypes;' not in new:
            new = re.sub(r'(import net\.minecraft\.world\.entity\.EntityType;\n)', r'\1import net.minecraft.world.entity.EntityTypes;\n', new, count=1)
            if 'import net.minecraft.world.entity.EntityTypes;' not in new:
                new = re.sub(r'^(package [\w.]+;\n)', r'\1\nimport net.minecraft.world.entity.EntityTypes;\n', new, count=1, flags=re.M)
        if 'setHudHidden(' in new and 'static void setHudHidden(' not in new:
            helper = ('\n\t/** 26.2: the HUD is hidden through Minecraft.gui.hud, which only toggles */\n'
                      '\tprivate static void setHudHidden(net.minecraft.client.Minecraft mc, boolean hidden)\n\t{\n'
                      '\t\tif(mc.gui.hud.isHidden() != hidden)\n\t\t\tmc.gui.hud.toggle();\n\t}\n')
            end = new.rstrip().rfind('}')
            new = new[:end].rstrip('\n') + '\n' + helper + '}\n'
        if new != text:
            path.write_text(new, encoding='utf-8')
            changed += 1
print(changed, 'files changed')
