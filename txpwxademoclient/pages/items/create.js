// pages/items/create.js
Page({
    data: {
        files: [],
        islongtap: false
    },
    chooseImage: function (e) {
        var that = this;
        wx.chooseImage({
            sizeType: ['original', 'compressed'], // 可以指定是原图还是压缩图，默认二者都有
            sourceType: ['album', 'camera'], // 可以指定来源是相册还是相机，默认二者都有
            success: function (res) {
                // 返回选定照片的本地文件路径列表，tempFilePath可以作为img标签的src属性显示图片
                that.setData({
                    files: that.data.files.concat(res.tempFilePaths)
                });
            }
        })
    },
    ontouchImage: function () {
        this.islongtap = false;
        console.log("touchstart");
    },
    previewImage: function (e) {
        if (this.islongtap) return;
        wx.previewImage({
            current: e.currentTarget.id, // 当前显示图片的http链接
            urls: this.data.files // 需要预览的图片http链接列表
        })
    },
    handleImage: function (e) {
        var that = this;

        this.islongtap = true;
        console.log('currentTarget: ', e.currentTarget);
        var currentTarget = e.currentTarget;
        wx.showActionSheet({
            itemList: ['删除'],
            itemColor: '#FF0000',
            success: function (res) {
                console.log(res.tapIndex)
                if (res.tapIndex == 0) {
                    var files = that.data.files;
                    var filetoremove = currentTarget.id;
                    var i = files.indexOf(filetoremove);
                    if (i != -1) {
                        files.splice(i, 1);
                        that.setData({
                            files: files
                        });
                    }
                }
            },
            fail: function (res) {
                console.log(res.errMsg)
            }
        })
    }
});