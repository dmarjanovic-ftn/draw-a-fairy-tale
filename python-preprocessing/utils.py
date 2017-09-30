from os import listdir
from os.path import isfile, join


def get_file_names_from_directory(directory):
    return [file_name for file_name in listdir(directory) if isfile(join(directory, file_name))]


def get_class_name(file_name):
    return file_name.split('.')[0]


def to_bitmap(x):
    if x > 127:
        return 1
    else:
        return 0
