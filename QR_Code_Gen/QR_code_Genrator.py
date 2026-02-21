import qrcode
url=input("Enter the URL or text to generate QR Code: ").strip()
filename=input("Enter the filename (without extension): ").strip()
file_path=f"C:\\Users\\Levono\\Documents\\{filename}.png"

qr=qrcode.QRCode()
qr.add_data(url)

img=qr.make_image(fill_color="black", back_color="white")
img.save(file_path)

print("QR Code generated and saved to", file_path)