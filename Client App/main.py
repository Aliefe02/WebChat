import curses
import time

def hex_to_curses(hex_color):
    r, g, b = int(hex_color[1:3], 16), int(hex_color[3:5], 16), int(hex_color[5:7], 16)
    return (r * 1000 // 255, g * 1000 // 255, b * 1000 // 255)

def chat_interface(stdscr):
    curses.curs_set(1)
    stdscr.nodelay(True)
    curses.start_color()
    curses.use_default_colors()

    r, g, b = hex_to_curses("#00ff00")
    curses.init_color(10, r, g, b)
    curses.init_pair(1, 10, -1)

    full_messages = [(1, "Welcome to the chat!")]
    display_messages = full_messages[-15:]
    input_text = ""
    message_id = 2
    scroll_step = 1
    history_index = len(full_messages)

    while True:
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

        key = input_win.getch()

        if key == curses.KEY_RESIZE:
            continue

        elif key == 10:
            if input_text.lower() == "exit":
                break
            full_messages.append((message_id, f"You: {input_text}"))
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
                    if display_messages[0][0] == full_messages[0][0]:
                        pass
                    elif history_index - scroll_step > 0:
                        history_index -= scroll_step
                        display_messages = full_messages[history_index - 15:history_index]
                    else:
                        display_messages = full_messages[:15]
                elif next_key == 66:
                    if history_index + scroll_step <= len(full_messages):
                        history_index += scroll_step
                        display_messages = full_messages[history_index - 15:history_index]
                    else:
                        display_messages = full_messages[-15:]

        elif 32 <= key <= 126:
            input_text += chr(key)

        curses.napms(50)

curses.wrapper(chat_interface)
