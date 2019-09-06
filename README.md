# 1.项目名称
mt3561低成本SystemUI

代码仓库：
git@172.168.0.64:Everest-Project/SystemUI_lowcost.git
# 2.分支说明
只有master分支，兼容1024&600和1280&720分辨率
# 3.功能描述
在原生systemui代码的基础上，将底部导航栏改为跳转到第三方应用，下拉菜单可以设置无线网络，移动网络，蓝牙，声音，亮度等
# 4.编译方式
ssh pengchong@172.168.1.20

密码：pengchong

cd mt3561

. build/envsetup.sh

lunch 25

cd frameworks/base/packages/SystemUI

mm

# 5.类说明

SystemUIApplication.java 继承Application,应用初始化

FlyaudioSystemUI.java 监听页面的第三方应用跳转的点击事件，跳转到指定的应用

IProxyConnet.aidl 定义调用系统的接口

PowerButton.java 所有快捷按钮wifi,蓝牙等的基础类

PowerWidget.java 下拉菜单快捷按钮自定义布局类，继承FrameLayout

PhoneStatusBar.java statusbar类，加载底部导航栏和状态栏

# 6.项目进度

完成




