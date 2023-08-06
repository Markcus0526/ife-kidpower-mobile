判断要安装的机win7版本并将相应的驱动copy到目录文件夹中.
将附件中的mdmcpq.inf/ble_dongle.inf解压到C:\windows\inf文件夹
将附件中的usbser.sys解压到C:\windows\system32\drivers文件夹
打开设备管理器更新驱动. 指定安装路径为带ble_dongle.inf文件的文件夹

*xp系统/win7 32bit系统, 先到win7 32bit中运行bat批处理文件. 安装驱动时候xp直接指到dongle文件夹，win7 32bit指到win7 32bit文件夹
*win7 64bit系统, 先运行win7 64bit中bat批处理文件, 安装时候使用win7 64bit文件夹.

