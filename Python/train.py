from ultralytics import YOLO
import torch
import torchvision

model = YOLO("yolo11s.pt")

if __name__ == '__main__':
    device = torch.device('cuda:0' if torch.cuda.is_available() else 'cpu')
    print(device)
    print(torch.cuda.is_available()) 
    print(torch.version.cuda)         
    print(torch.__version__)
    print(torchvision.__version__)
    # print("Device Name:", torch.cuda.get_device_name(0))
    results = model.train(
        data='datasets/data.yaml', 
        epochs=300,
        batch=4,
        device=device,
        )  
    
    metrics = model.val()
#     # # results = model("path/to/image.jpg")
#     # # results[0].show()

    model.export(format='onnx')
    model.export(format='tflite')
#     # model.export(format='coreml')




