from os import listdir
from os.path import isfile, join
from random import random


def is_test():
    return random() < .2


def get_file_names_from_directory(directory):
    return [file_name for file_name in listdir(directory) if isfile(join(directory, file_name))]


def get_class_name(file_name):
    return file_name.split('.')[0]
