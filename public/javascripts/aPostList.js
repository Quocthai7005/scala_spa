$(document).ready(function() {

    var postList = new PostList();
    postList.initDeletePost();
});

function PostList() {
    var self = this;

    self.initDeletePost = function() {
        $('.post-delete').on('click', function() {
            swal({
                text: "Bạn muốn xoá tin này?",
                type: 'warning',
                showCancelButton: true,
                confirmButtonText: "Có",
                cancelButtonText: "Không",
            }).then((result) => {
                if (result.value) {
                    var id = $(this).attr('data-id')
                    jsRoutes.controllers.PostController.delete(id).ajax({
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