$(document).ready(function() {

    var serviceGrpList = new ServiceGrpList();
    serviceGrpList.initDeleteGroup();
});

function ServiceGrpList() {
    var self = this;

    self.initDeleteGroup = function() {
        $('.group-delete').on('click', function() {
            swal({
                text: "Bạn muốn xoá nhóm dịch vụ này?",
                type: 'warning',
                showCancelButton: true,
                confirmButtonText: "Có",
                cancelButtonText: "Không",
            }).then((result) => {
                if (result.value) {
                    var id = $(this).attr('data-id')
                    jsRoutes.controllers.ServiceGrpController.delete(id).ajax({
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
                                    footer: 'Bạn đã xoá tất cả dịch vụ chưa?',
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
            })
        })
    }
}