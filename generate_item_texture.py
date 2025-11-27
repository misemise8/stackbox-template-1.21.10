from PIL import Image, ImageDraw

# Create a 16x16 image for the item
size = 16
img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
draw = ImageDraw.Draw(img)

# Vanilla chest-style cardboard box colors
wood_top = (218, 178, 130, 255)
wood_front = (190, 150, 102, 255)
wood_side = (164, 128, 86, 255)
wood_dark = (138, 106, 70, 255)
wood_shadow = (112, 84, 54, 255)
outline = (88, 66, 42, 255)
lock_gold = (255, 220, 120, 255)
lock_dark = (180, 140, 60, 255)

# Draw chest-like box (centered and taller)
# Top face (visible from front)
draw.polygon([(4, 3), (12, 3), (13, 2), (3, 2)], fill=wood_top)
draw.line([(3, 2), (13, 2)], fill=outline)
draw.line([(13, 2), (12, 3)], fill=outline)
draw.line([(3, 2), (4, 3)], fill=outline)

# Main front face (taller)
draw.rectangle([4, 3, 12, 13], fill=wood_front)

# Left side edge
draw.polygon([(3, 2), (4, 3), (4, 13), (3, 14)], fill=wood_side)

# Right side edge
draw.polygon([(13, 2), (12, 3), (12, 13), (13, 14)], fill=wood_dark)

# Bottom
draw.polygon([(3, 14), (4, 13), (12, 13), (13, 14)], fill=wood_shadow)

# Outline the box
draw.rectangle([4, 3, 12, 13], outline=outline)
draw.line([(3, 2), (3, 14)], fill=outline)
draw.line([(13, 2), (13, 14)], fill=outline)
draw.line([(3, 14), (13, 14)], fill=outline)

# Add wood planks texture (horizontal lines like chest)
for y in [5, 7, 9, 11]:
    draw.line([(5, y), (11, y)], fill=wood_dark)
    draw.line([(5, y+1), (11, y+1)], fill=(200, 160, 112, 255))

# Add vertical wood detail
draw.line([(8, 4), (8, 12)], fill=wood_dark)

# Lock/Clasp in the center (like chest latch)
# Lock base
draw.rectangle([7, 7, 9, 9], fill=lock_dark)
draw.rectangle([7, 7, 8, 8], fill=lock_gold)

# Lock highlight
img.putpixel((7, 7), (255, 240, 150, 255))

# Add corner details
img.putpixel((5, 4), wood_top)
img.putpixel((11, 4), wood_top)

# Add side shading
draw.line([(4, 4), (4, 12)], fill=wood_side)
draw.line([(12, 4), (12, 12)], fill=wood_shadow)

# Top edge highlight
draw.line([(4, 3), (12, 3)], fill=(228, 188, 140, 255))

# Optional: Add small "STACK" text or icon
# Simple stacked rectangles icon (top left corner)
for x in [5, 6]:
    img.putpixel((x, 4), outline)
for x in [5, 6, 7]:
    img.putpixel((x, 5), outline)

# Save the image
img.save('stack_box_item.png')
print("Vanilla chest-style box texture generated: stack_box_item.png")
print(f"Size: {size}x{size} pixels")
print("Style: Centered, taller chest-like cardboard box")
print("Features: Wood plank texture, central lock/clasp")
print("Copy this file to: src/main/resources/assets/stackbox/textures/item/stack_box.png")