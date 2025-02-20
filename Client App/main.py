import websockets.connection
import websockets.exceptions
from ipcqueue import posixmq
import websockets
import threading
import requests
import asyncio
import getpass
import curses
import queue
import json
import os



API_DNS = "localhost:8080/"
API_URL = "http://" + API_DNS
REGISTER_URL = API_URL + "user/register"
LOGIN_URL = API_URL + "user/login"
DETAILS_URL = API_URL + "user/details"
EXISTS_URL = API_URL + "user/exists?username="
WS_URL = "ws://" + API_DNS + "ws/chat"

FULL_MESSAGES = list()



def hex_to_curses(hex_color):
    r, g, b = int(hex_color[1:3], 16), int(hex_color[3:5], 16), int(hex_color[5:7], 16)
    return (r * 1000 // 255, g * 1000 // 255, b * 1000 // 255)

def chat_interface(stdscr, full_messages, chat_user):
    recv_queue = posixmq.Queue('/recv_queue')
    send_queue = posixmq.Queue('/send_queue')
    
    curses.curs_set(1)
    # stdscr.nodelay(True)
    curses.start_color()
    curses.use_default_colors()

    r, g, b = hex_to_curses("#00ff00")
    curses.init_color(10, r, g, b)
    curses.init_pair(1, 10, -1)

    display_messages = full_messages[-15:]
    input_text = ""
    message_id = len(full_messages) + 1
    history_index = len(full_messages)
    
    while True:
        try:
            while recv_queue.qsize() > 0:
                try:
                    message = recv_queue.get_nowait()
                    full_messages.append((message_id, f"{chat_user}: {message}"))
                    message_id += 1
                    display_messages = full_messages[-15:]
                except queue.Empty:
                    break

            stdscr.clear()
            max_y, max_x = stdscr.getmaxyx()

            chat_win = stdscr.subwin(max_y - 2, max_x, 0, 0)
            input_win = stdscr.subwin(1, max_x, max_y - 1, 0)

            chat_win.scrollok(True)
            

            chat_win.clear()
            for i, (_, msg) in enumerate(display_messages[-(max_y - 3):]):
                chat_win.addstr(i, 0, msg[:max_x-1], curses.color_pair(1))
            chat_win.refresh()

            input_win.clear()
            input_win.addstr("Type here: " + input_text, curses.color_pair(1))
            input_win.refresh()

            input_win.timeout(100)
            key = input_win.getch()

            if key == -1:
                continue

            if key == curses.KEY_RESIZE:
                continue

            elif key == 10:
                if input_text.lower() == "!exit":
                    break
                full_messages.append((message_id, f"You: {input_text}"))
                send_queue.put(input_text)
                message_id += 1
                display_messages = full_messages[-15:]
                history_index = len(full_messages)
                input_text = ""

            elif key == 127:
                input_text = input_text[:-1]

            elif key == 27:
                next_key = input_win.getch()
                if next_key == 91:
                    next_key = input_win.getch()
                    if next_key == 65:
                        if history_index > 15:
                            history_index -= 1
                            display_messages = full_messages[history_index - 15:history_index]
                    elif next_key == 66:
                        if history_index < len(full_messages):
                            history_index += 1
                            display_messages = full_messages[history_index - 15:history_index]

            elif 32 <= key <= 126:
                input_text += chr(key)

            curses.napms(50)
        except KeyboardInterrupt:
            break

    os.system("stty sane")

def set_token(token):
    return {"Authorization": "Bearer " + token}


def login():
    while True:
        try:
            username = input("Username > ")
            password = getpass.getpass("Password > ")
            body = {"username": username, "password": password}

            r = requests.post(LOGIN_URL, json=body)

            if r.status_code == 200:
                return set_token(r.text), username

            elif r.status_code == 403:
                print("Username or password incorrect")
        except:
            return None, None


def register():
    while True:
        try:
            username = input("Username > ")
            password = getpass.getpass("Password > ")
            first_name = input("First name > ")
            last_name = input("Last name > ")

            body = {
                "username": username,
                "password": password,
                "firstName": first_name,
                "lastName": last_name
            }

            r = requests.post(REGISTER_URL, json=body)

            if r.status_code == 201:
                return set_token(r.text), username

            elif r.status_code == 403:
                print(r.text)
            else:
                print(r.status_code)
        except:
            return None, None


def user_exists(check_username, headers):
    r = requests.get(EXISTS_URL + check_username, headers=headers)
    return r.status_code == 200

async def websocket_handler(uri, headers, chat_user):
    async with websockets.connect(uri, extra_headers=headers) as ws:
        
        async def receive_messages():
            recv_queue = posixmq.Queue('/recv_queue')
            while True:
                try:
                    message = await asyncio.wait_for(ws.recv(), timeout=5)
                    recv_queue.put(message)
                except asyncio.TimeoutError:
                    continue
                except websockets.exceptions.ConnectionClosed:
                    recv_queue.put("Connection closed")
                    break

        async def send_messages():
            send_queue = posixmq.Queue('/send_queue')
            while True:
                try:
                    msg = send_queue.get_nowait()
                    if msg.lower() == "!exit":
                        await ws.close()
                        break
                    data = json.dumps({
                        "recipient": chat_user,
                        "message": msg
                    })
                    await ws.send(data)
                except queue.Empty:
                    await asyncio.sleep(0.1)

        await asyncio.gather(receive_messages(), send_messages())

def create_websocket(headers, chat_user):
    thread = threading.Thread(target=lambda: asyncio.run(websocket_handler(WS_URL, headers, chat_user)), daemon=True)
    thread.start()


def clear_screen():
    os.system('cls' if os.name == 'nt' else 'clear')
    print("------------------")
    print(f"Welcome {username}")
    print("------------------")
    print("Enter command (!chat, !logout, !clear, !quit)")



logged_in = False
username = None
headers = dict()

print("Enter command (login, register, !quit)")
while True:
    try:
        user_input = input("> ").strip().lower()
        if user_input == "login":
            headers, username = login()

        elif user_input == "register":
            headers, username = register()

        elif user_input == "!quit":
            logged_in = False
            break

        if headers:
            logged_in = True
            break

    except KeyboardInterrupt:
        print()
        exit()

if logged_in:
    clear_screen()
    while True:
        try:
            user_input = input("> ").strip().lower()
            if user_input == "!chat":
                while True:
                    chat_user = input("Enter a user to chat (!back) > ")
                    if chat_user.lower() == "!back":
                        break
                    elif user_exists(chat_user, headers):
                        full_messages = [(1, f"New chat with {chat_user}")]
                        create_websocket(headers, chat_user)
                        curses.wrapper(chat_interface, full_messages,chat_user)
                        clear_screen()
                    else:
                        print("User does not exist, try again")

            elif user_input == "!clear":
                clear_screen()

            elif user_input == "!quit":
                break

            else:
                print("Unknown command. Use !chat to open chat or !quit to exit.")

        except KeyboardInterrupt:
            print()
            break
