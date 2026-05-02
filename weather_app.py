"""Weather Now — 便携桌面应用启动器"""
import http.server
import os
import socket
import subprocess
import sys
import threading
import webbrowser

# PyInstaller 打包后的实际目录
if getattr(sys, 'frozen', False):
    APP_DIR = os.path.dirname(sys.executable)
else:
    APP_DIR = os.path.dirname(os.path.abspath(__file__))
HOST = "127.0.0.1"

def find_edge():
    paths = [
        os.path.join(os.environ.get("ProgramFiles(x86)", ""), "Microsoft", "Edge", "Application", "msedge.exe"),
        os.path.join(os.environ.get("ProgramFiles", ""), "Microsoft", "Edge", "Application", "msedge.exe"),
    ]
    for p in paths:
        if os.path.isfile(p):
            return p
    return None

def find_free_port():
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.bind((HOST, 0))
        return s.getsockname()[1]

def start_server(port):
    handler = http.server.SimpleHTTPRequestHandler
    os.chdir(APP_DIR)
    httpd = http.server.HTTPServer((HOST, port), handler)
    httpd.serve_forever()

def main():
    port = find_free_port()
    url = f"http://{HOST}:{port}/index.html"

    # 后台启动本地 HTTP 服务器
    t = threading.Thread(target=start_server, args=(port,), daemon=True)
    t.start()

    edge = find_edge()
    if edge:
        subprocess.Popen([edge, f"--app={url}"], creationflags=subprocess.CREATE_NO_WINDOW)
    else:
        webbrowser.open(url)

    # 保持进程存活直到 Edge 窗口关闭
    try:
        while True:
            import time
            time.sleep(1)
    except KeyboardInterrupt:
        pass

if __name__ == "__main__":
    main()
