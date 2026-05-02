"""Weather Now — 便携桌面应用启动器"""
import os
import subprocess
import sys

def find_edge():
    for p in [
        os.path.join(os.environ.get("ProgramFiles(x86)", ""), "Microsoft", "Edge", "Application", "msedge.exe"),
        os.path.join(os.environ.get("ProgramFiles", ""), "Microsoft", "Edge", "Application", "msedge.exe"),
    ]:
        if os.path.isfile(p):
            return p
    return None

def main():
    if getattr(sys, 'frozen', False):
        app_dir = os.path.dirname(sys.executable)
    else:
        app_dir = os.path.dirname(os.path.abspath(__file__))

    index_path = os.path.join(app_dir, "index.html")

    if not os.path.isfile(index_path):
        import tkinter.messagebox as mb
        mb.showerror("错误", f"找不到 index.html\n期望路径: {index_path}")
        return

    edge = find_edge()
    if edge:
        subprocess.Popen([
            edge,
            f"--app=file:///{index_path.replace(os.sep, '/')}",
        ], creationflags=subprocess.CREATE_NO_WINDOW)
    else:
        os.startfile(index_path)

if __name__ == "__main__":
    main()
