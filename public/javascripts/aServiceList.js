$(document).ready(function() {

    var serviceDtlList = new ServiceDtlList();
    serviceDtlList.initDeleteService();
});

function ServiceDtlList() {
    var self = this;

    self.initDeleteService = function() {
        $('.service-delete').on('click', function() {
            swal({
                text: "Bạn muốn xoá dịch vụ này?",
                type: 'warning',
                showCancelButton: true,
                confirmButtonText: "Có",
                cancelButtonText: "Không"
            }).then((result) => {
                if (result.value) {
                    var id = $(this).attr('data-id')
                    jsRoutes.controllers.ServiceDtlController.delete(id).ajax({
                        success: function (data) {
                            if (data == 'success') {
                                swal({
                                    type: 'success',
                                    text: 'Xoá thành công',
                                    onClose: function() {
                                        location.reload();
                                    }
                                });
                            } else {
                                swal({
                                    type: 'error',
                                    text: 'Không thể xoá',
                                });
                            }
                        },
                        error: function (e) {
                            swal({
                                type: 'error',
                                text: 'Không thể xoá',
                            });
                        }
                    })
                }
            });
        })
    }
}