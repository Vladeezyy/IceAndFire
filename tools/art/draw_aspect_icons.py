import sys, math
from PIL import Image, ImageDraw
S = 4  # drawn at 128 px, scaled to 32
OUT = sys.argv[1]
BLACK, WHITE, GREY = (0, 0, 0, 255), (255, 255, 255, 255), (170, 170, 170, 255)

def finish(img, name):
    img.resize((32, 32), Image.LANCZOS).save(f"{OUT}/{name}.png")
    img.resize((32, 32), Image.LANCZOS).resize((128, 128), Image.NEAREST).save(f"{OUT}/{name}_preview.png")

def ring(d):
    d.ellipse((6, 6, 122, 122), fill=BLACK)
    d.ellipse((14, 14, 114, 114), fill=WHITE)
    d.ellipse((22, 22, 106, 106), fill=BLACK)

# Fulgur: a lightning bolt in a ring (like Ignis)
img = Image.new("RGBA", (128, 128), (0, 0, 0, 0)); d = ImageDraw.Draw(img)
ring(d)
d.ellipse((26, 26, 102, 102), fill=GREY)
bolt = [(72, 24), (42, 70), (62, 70), (50, 106), (88, 54), (67, 54), (80, 24)]
d.polygon(bolt, fill=BLACK)
inner = [(73, 31), (50, 65), (68, 65), (59, 92), (80, 59), (60, 59), (75, 31)]
d.polygon(inner, fill=WHITE)
finish(img, "fulgur")

# Draco: a dragon's eye with a slit pupil and three horns/spines above it
img = Image.new("RGBA", (128, 128), (0, 0, 0, 0)); d = ImageDraw.Draw(img)
# spines
for x0, top in ((20, 26), (48, 6), (78, 18)):
    d.polygon([(x0, 58), (x0 + 34, 52), (x0 + 30, top)], fill=BLACK)
    d.polygon([(x0 + 9, 52), (x0 + 27, 49), (x0 + 26, top + 16)], fill=GREY)
# almond eye: two arcs
def almond(cx, cy, w, h):
    pts = []
    for i in range(0, 181, 6):
        a = math.radians(i)
        pts.append((cx - w * math.cos(a), cy - h * math.sin(a)))
    for i in range(0, 181, 6):
        a = math.radians(i)
        pts.append((cx + w * math.cos(a), cy + h * math.sin(a)))
    return pts
d.polygon(almond(64, 80, 60, 40), fill=BLACK)
d.polygon(almond(64, 80, 50, 31), fill=WHITE)
d.ellipse((40, 56, 88, 104), fill=GREY)
d.ellipse((46, 62, 82, 98), fill=WHITE)
d.polygon(almond(64, 80, 30, 8)[::1], fill=None)
# slit pupil (vertical)
pupil = [(64, 50), (72, 80), (64, 110), (56, 80)]
d.polygon(pupil, fill=BLACK)
d.polygon(almond(64, 80, 50, 31), outline=BLACK)
finish(img, "draco")
