from PIL import Image, ImageDraw

# Create a 16x16 image for the item
size = 16
img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
draw = ImageDraw.Draw(img)

# Box colors
brown_dark = (101, 67, 33, 255)
brown_mid = (139, 90, 43, 255)
brown_light = (180, 120, 60, 255)

# Draw a simple box/chest icon
# Bottom (dark)
draw.rectangle([2, 11, 13, 14], fill=brown_dark)

# Front face
draw.rectangle([2, 5, 13, 10], fill=brown_mid)

# Top (light)
draw.rectangle([2, 2, 13, 4], fill=brown_light)

# Add some detail lines
draw.line([7, 2, 7, 14], fill=brown_dark)
draw.line([8, 2, 8, 14], fill=brown_light)

# Lock/latch
draw.rectangle([7, 7, 8, 8], fill=(200, 200, 0, 255))

# Border
draw.rectangle([2, 2, 13, 14], outline=(80, 50, 25, 255))

# Save the image
img.save('stack_box_item.png')
print("Item texture generated: stack_box_item.png")
print(f"Size: {size}x{size} pixels")
print("Copy this file to: src/main/resources/assets/stackbox/textures/item/stack_box.png")