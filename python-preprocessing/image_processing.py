from utils import is_test, get_file_names_from_directory
from scipy.misc import imsave
import numpy as np


class ImageProcessing(object):

    def __init__(self, data_path='quick_draw_data', processed='data'):
        self._root_data = data_path
        self._processed = processed

    def get_files(self):
        return get_file_names_from_directory(self._root_data)

    def load_images(self, name):
        return np.load(self._root_data + '/' + name)

    def save_image(self, name, array):
        if is_test():
            save_path = self._processed + '/train/' + name + '.jpg'
        else:
            save_path = self._processed + '/test/' + name + '.jpg'

        imsave(save_path, ImageProcessing._process_array(array))

    @staticmethod
    def _process_array(array, width=28, height=28):
        return np.reshape(array, (width, height))
