from image_processing import ImageProcessing
from utils import get_class_name

if __name__ == "__main__":

    class_names = [
        'cello',
        'clarinet',
        'drums',
        'guitar',
        'harp',
        'piano',
        'saxophone',
        'trombone',
        'trumpet',
        'violin'
    ]

    processing = ImageProcessing(class_names, max_class_data=22000, train_data=20000, binarized=True,
                                 generate_images=True, init_dirs=True)

    classes = processing.get_files()
    for c in classes:
        class_name = get_class_name(c)
        numpy_data = processing.load_images(c)
        data_no = len(numpy_data)

        if data_no > 0:
            print "Processing " + class_name + "..."

        # Generate only maximum 'max_class_data' images
        for i in xrange(min(processing.max_class_data, data_no)):
            processing.save_image(name=class_name, index=i, array=numpy_data[i], is_test=i >= processing.train_data)

    processing.to_k_fold_sets(k=10, set_names=class_names)
