# Client AATool
#### A client-side fabric mod which allows you to use [AATool](https://github.com/DarwinBaker/AATool) on servers without FTP access

---
## How to Use
Follow instructions on how to install fabric mods [here](https://fabricmc.net/use/) and put [Client_AATool-x.x.jar](https://github.com/fxmorin/Client_AATool/releases) in the `mods` folder along with other compatible mods.

To use the mod, simple run `/aatool true` when on a server, and the mod will make a fake directory that can be used for [AATool](https://github.com/DarwinBaker/AATool)
You need to run this command every time you restart your client, this is so you don't forget that it's running!

Make sure you are running minecraft in Admin Mode if the folder is not being created

### Setting up AATool
1. When opening AATool, head over to settings (*Bottom Left corner*)
2. You will see `Local Save Folder` Select `Use Custom Save Folder`
3. Put Your .minecraft folder location + `client_aatool`

It should look something like: `C:\Users\<username>\AppData\Roaming\.minecraft\client_aatool`

## How does this mod work?
It's actually pretty simple. The client receives the information that would normally be in the save files through packets.
We basically intercept the packets and write the data into a file in the format that AATool would recognize.

---

This was created for another project *no secrets* ;)
