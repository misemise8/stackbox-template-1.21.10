from PIL import Image, ImageDraw

# Create a 176x166 image (standard Minecraft GUI size)
width = 176
height = 166
img = Image.new('RGBA', (width, height), (0, 0, 0, 0))
draw = ImageDraw.Draw(img)

# Background color (dark gray)
bg_color = (198, 198, 198, 255)
border_dark = (55, 55, 55, 255)
border_light = (255, 255, 255, 255)
slot_bg = (139, 139, 139, 255)

# Draw main background
draw.rectangle([0, 0, width-1, height-1], fill=bg_color)

# Draw outer border (3D effect)
# Top and left (dark)
draw.line([0, 0, width-1, 0], fill=border_light)  # Top
draw.line([0, 0, 0, height-1], fill=border_light)  # Left
# Bottom and right (light)
draw.line([0, height-1, width-1, height-1], fill=border_dark)  # Bottom
draw.line([width-1, 0, width-1, height-1], fill=border_dark)  # Right

# Draw slot for stored item (centered at top)
slot_x = 79
slot_y = 19
slot_size = 18

# Slot border (3D inset effect)
draw.rectangle([slot_x-1, slot_y-1, slot_x+slot_size, slot_y+slot_size], fill=border_dark)
draw.line([slot_x+slot_size, slot_y-1, slot_x+slot_size, slot_y+slot_size], fill=border_light)
draw.line([slot_x-1, slot_y+slot_size, slot_x+slot_size, slot_y+slot_size], fill=border_light)
draw.rectangle([slot_x, slot_y, slot_x+slot_size-1, slot_y+slot_size-1], fill=slot_bg)

# Draw player inventory area
inventory_x = 7
inventory_y = 83

# Draw 3 rows of 9 slots (main inventory)
for row in range(3):
    for col in range(9):
        sx = inventory_x + col * 18
        sy = inventory_y + row * 18

        # Slot border
        draw.rectangle([sx, sy, sx+17, sy+17], outline=border_dark)
        draw.line([sx+17, sy, sx+17, sy+17], fill=border_light)
        draw.line([sx, sy+17, sx+17, sy+17], fill=border_light)
        draw.rectangle([sx+1, sy+1, sx+16, sy+16], fill=slot_bg)

# Draw hotbar (9 slots)
hotbar_y = inventory_y + 58
for col in range(9):
    sx = inventory_x + col * 18
    sy = hotbar_y

    # Slot border
    draw.rectangle([sx, sy, sx+17, sy+17], outline=border_dark)
    draw.line([sx+17, sy, sx+17, sy+17], fill=border_light)
    draw.line([sx, sy+17, sx+17, sy+17], fill=border_light)
    draw.rectangle([sx+1, sy+1, sx+16, sy+16], fill=slot_bg)

# Save the image
img.save('stack_box_gui.png')
print("GUI texture generated: stack_box_gui.png")
print(f"Size: {width}x{height} pixels")
print("Copy this file to: src/main/resources/assets/stackbox/textures/gui/stack_box.png")