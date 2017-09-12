from utils import is_test, get_file_names_from_directory, to_bitmap
from scipy.misc import imsave
import json
import numpy as np


class ImageProcessing(object):

    def __init__(self, data_path='quick_draw_data', processed='data', max_class_data=10000, generate_images=False, binarized=False):
        self.max_class_data = max_class_data
        self._root_data = data_path
        self._processed = processed
        self._generate_images = generate_images
        self._binarized = binarized

    def get_files(self):
        return get_file_names_from_directory(self._root_data)

    def load_images(self, name):
        try:
            return np.load(self._root_data + '/' + name)
        except IOError:
            print "Can't open " + name + " file!"
            return []

    def save_image(self, name, index, array):
        if self._generate_images:
            if is_test():
                save_path = self._processed + '/test/' + name + '_' + str(index) + '.jpg'
            else:
                save_path = self._processed + '/train/' + name + '_' + str(index) + '.jpg'

            imsave(save_path, ImageProcessing._process_array(array))
        else:
            if self._binarized:
                pixels = map(lambda _: to_bitmap(_), array.tolist())
            else:
                pixels = array.tolist()

            drawing = json.dumps({
                'drawing': pixels,
                'word': name
            })

            with open(self._processed + '/processed/' + name + '.json', "a") as f:
                f.write(drawing + '\n')

    @staticmethod
    def _process_array(array, width=28, height=28):
        return np.reshape(array, (width, height))
