import asyncio
import json
from aiohttp import web


async def ws_handler(request: web.Request) -> web.WebSocketResponse:
    ws = web.WebSocketResponse()
    await ws.prepare(request)
    async for msg in ws:
        if msg.type == web.WSMsgType.TEXT:
            await ws.send_str(msg.data)
        elif msg.type == web.WSMsgType.ERROR:
            break
    return ws


async def sse_handler(request: web.Request) -> web.StreamResponse:
    response = web.StreamResponse(
        status=200,
        headers={
            "Content-Type": "text/event-stream",
            "Cache-Control": "no-cache",
            "Connection": "keep-alive",
        },
    )
    await response.prepare(request)
    await response.write(b"id: 1\n")
    await response.write(b"event: update\n")
    await response.write(b"data: {\"id\":7}\n\n")
    await response.write_eof()
    return response


async def poll_handler(request: web.Request) -> web.Response:
    return web.json_response({"id": 11})


app = web.Application()
app.add_routes([
    web.get("/ws", ws_handler),
    web.get("/sse", sse_handler),
    web.get("/poll", poll_handler),
])


if __name__ == "__main__":
    web.run_app(app, host="0.0.0.0", port=18080)
