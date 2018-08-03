$(document).ready(function() {

    var common = new Common();
    common.initEditor();
    common.initImgConvert();
    common.initImgDisplay();

})

function Common() {
    var self = this;

    self.initEditor = function() {
        tinymce.init({
            selector: 'textarea',
            plugins: ['image', 'table'],
            toolbar: [
                ' bold italic underline | alignleft aligncenter alignright alignjustify | fontselect | fontsizeselect | table | image '
            ],
            fontsize_formats: '8pt 10pt 12pt 14pt 18pt 24pt 36pt',
            height : '480'
        });
    }

    self.initImgConvert = function() {
        $('#select-image').change(function() {
            var reader = new FileReader();
            reader.onload = function (e) {
                var iSize = ($('#select-image')[0].files[0].size / 1024);
                if (iSize > 100) {
                    swal({
                        type: 'error',
                        title: 'Oops...',
                        text: 'Vui lòng chọn hình có kích thước nhỏ hơn 100kb',
                    })
                } else {
                    $('#image').val(e.target.result);
                }
            };
            reader.readAsDataURL($('#select-image')[0].files[0]);
        });
    }

    self.initImgDisplay = function() {
        $('.see-image').on('click', function() {
            var img = $('#image').val();
            if(img) {
                swal({
                    imageUrl: img,
                    width: 250,
                    imageHeight: 200,
                    imageWidth: 200,
                });
            } else {
                swal({
                    width: 250,
                    type: 'error',
                    title: 'Oops...',
                    text: 'Vui lòng chọn hình!',
                })
            }
        });
    };
}