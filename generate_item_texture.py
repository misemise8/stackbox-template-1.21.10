from PIL import Image, ImageDraw

# Create a 16x16 image for the item
size = 16
img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
draw = ImageDraw.Draw(img)

# Box colors inspired by the uploaded image - simple brown/tan box
box_top = (220, 180, 130, 255)
box_front = (190, 150, 100, 255)
box_side = (160, 120, 80, 255)
box_dark = (130, 90, 60, 255)
outline = (80, 60, 40, 255)
infinity_gold = (255, 220, 100, 255)
infinity_shadow = (200, 160, 60, 255)

# Draw 3D box similar to uploaded image
# Top face
draw.polygon([(5, 4), (11, 4), (13, 2), (3, 2)], fill=box_top)
draw.line([(3, 2), (13, 2), (11, 4), (5, 4), (3, 2)], fill=outline)

# Front face
draw.rectangle([5, 4, 11, 12], fill=box_front)
draw.rectangle([5, 4, 11, 12], outline=outline)

# Left side
draw.polygon([(3, 2), (5, 4), (5, 12), (3, 14)], fill=box_side)
draw.line([(3, 2), (5, 4), (5, 12), (3, 14), (3, 2)], fill=outline)

# Right side
draw.polygon([(11, 4), (13, 2), (13, 14), (11, 12)], fill=box_dark)
draw.line([(11, 4), (13, 2), (13, 14), (11, 12), (11, 4)], fill=outline)

# Bottom edge
draw.line([(3, 14), (13, 14)], fill=outline)

# Draw infinity symbol (∞) on the front face
# Simple pixel-art infinity symbol
# Left loop
draw.point((6, 7), fill=infinity_gold)
draw.point((7, 6), fill=infinity_gold)
draw.point((7, 8), fill=infinity_gold)
draw.point((6, 9), fill=infinity_gold)

# Center connection
draw.point((8, 7), fill=infinity_gold)
draw.point((8, 8), fill=infinity_gold)

# Right loop
draw.point((9, 6), fill=infinity_gold)
draw.point((9, 8), fill=infinity_gold)
draw.point((10, 7), fill=infinity_gold)
draw.point((10, 9), fill=infinity_gold)

# Add shadow to infinity symbol
draw.point((6, 8), fill=infinity_shadow)
draw.point((10, 8), fill=infinity_shadow)

# Save the image
img.save('stack_box_item.png')
print("16x16 3D box texture with infinity symbol generated!")
print(f"Size: {size}x{size} pixels")
print("Style: 3D isometric box similar to uploaded image with infinity symbol")
print("Copy this file to: src/main/resources/assets/stackbox/textures/item/stack_box.png")