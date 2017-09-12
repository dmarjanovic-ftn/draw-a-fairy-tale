from image_processing import ImageProcessing
from utils import get_class_name

if __name__ == "__main__":
    processing = ImageProcessing()

    classes = processing.get_files()
    for c in classes:
        class_name = get_class_name(c)
        numpy_data = processing.load_images(c)
        data_no = len(numpy_data)

        print "Processing " + class_name + "..."

        # Generate only 1000 images
        for i in xrange(min(1000, data_no)):
            processing.save_image(class_name + ' ' + str(i), numpy_data[i])
