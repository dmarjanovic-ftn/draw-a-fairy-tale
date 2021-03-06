# Data Preprocessing

Project written in `Python Programming Language` which purpose is to prepare data for _clustering processing_ with `Spark`. 

## Running

Project is adapted to `NumPy` **datasets** downloaded from [this repo](https://console.cloud.google.com/storage/browser/quickdraw_dataset/full/numpy_bitmap). Dataset contains 345 different classes where total size of data is about **40 GB**.
Script is able to generate `28x28` images and saves it. Also, you can _config_ script to _generate_ `.ndjson` containing only **binarized** or **grayscale** pixels and **drawing class** which is convenient for training **neural network**. 

In order to run this application you need to follow these steps:

- Provide your data in `quick_draw_data` directory inside `python-preprocessing` directory or **provide root path** in `main.py` when creating `ImageProcessing`.
- Set _config_ if you need `.ndjson` or **images creation**.
- Run `Python` script via `python main.py` or through IDE. 

Additional configuration is where your **generated files** will be stored (_default_ path is under `python-processing` directory in `data` directory). Like we said earlier - it will generate both `train` and `test` data in ratio **80:20**. 
Also, you can setup **maximum number of generated values** for each **class** where _default_ value is **10000**.
