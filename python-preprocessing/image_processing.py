from utils import get_file_names_from_directory, to_bitmap
from scipy.misc import imsave
import json
import numpy as np


class ImageProcessing(object):
    def __init__(self, data_path='quick_draw_data', processed='data', max_class_data=10000,
                 generate_images=False, train_data=8000, binarized=False):
        self.max_class_data = max_class_data
        self.train_data = train_data
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

    def save_image(self, name, index, array, is_test=False):
        if self._generate_images:
            if is_test:
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

            if is_test:
                with open(self._processed + '/processed/test.json', "a") as f:
                    f.write(drawing + '\n')
            else:
                with open(self._processed + '/processed/' + name + '.json', "a") as f:
                    f.write(drawing + '\n')

    def to_k_fold_sets(self, k=10, set_names=None):
        validation_data = self.train_data / k
        for i in xrange(k):
            train_file = open(self._processed + '/k-fold/train_' + str(i) + '.json', "w")
            validation_file = open(self._processed + '/k-fold/validation_' + str(i) + '.json', "w")

            for name in set_names:
                with open(self._processed + '/processed/' + name + '.json') as f:
                    lines = f.readlines()
                    for (index, line) in zip(xrange(len(lines)), lines):
                        if i * validation_data < index <= (i + 1) * validation_data:
                            validation_file.write(line)
                        else:
                            train_file.write(line)
                f.close()

            train_file.close()
            validation_file.close()

    @staticmethod
    def _process_array(array, width=28, height=28):
        return np.reshape(array, (width, height))
