## undertow_websockets

Using this to experiment with websockets, and their behavior when clients go offline. What I notice currently: 

**Chrome: When a client goes offline, Undertow does not close the connection.**

## To test

### Run the clojure server

```bash
cd clojure_server
clj -M:run
# endpoint now live on ws://localhost:8888/ws
```

### Run the frontend

```bash
cd frontend
npm i
npm run dev
# frontend live on http://localhost:5173/
```

You'll now see a page to test the socket out. 

<img src="ex.png" alt="how the test looks" style="max-width: 500px; display: block; margin-left: auto; margin-right: auto;">

### Try to go offline

1. If you turn off your wifi, the connection status stays 'Connected' 
2. If you look at the backend, the server _still_ holds the websocket. No 'close' frame was ever sent
