import { useState, useEffect, useRef } from 'react'
import './App.css'

function App() {
  const [connectionState, setConnectionState] = useState<string>('Disconnected')
  const [messages, setMessages] = useState<string[]>([])
  const [inputMessage, setInputMessage] = useState<string>('')
  const socketRef = useRef<WebSocket | null>(null)

  useEffect(() => {
    if (!socketRef.current) {
      const socket = new WebSocket('ws://localhost:8888/ws')

      socket.onopen = () => setConnectionState('Connected')
      socket.onclose = () => setConnectionState('Disconnected')
      socket.onerror = () => setConnectionState('Error')
      socket.onmessage = (event) => {
        setMessages(prev => [...prev, `Received: ${event.data}`])
      }

      socketRef.current = socket
    }
  }, [])

  const sendMessage = () => {
    if (socketRef.current && socketRef.current.readyState === WebSocket.OPEN) {
      socketRef.current.send(inputMessage)
      setMessages(prev => [...prev, `Sent: ${inputMessage}`])
      setInputMessage('')
    }
  }

  return (
    <div className="App">
      <h1>WebSocket Tester</h1>
      <div>
        <p>1. Open your inspector. Select 'Go Offline'.</p>
        <p>2. Notice that the socket _does not_ close on the server</p>
      </div>
      <p>Connection State: {connectionState}</p>
      <div>
        <input
          type="text"
          value={inputMessage}
          onChange={(e) => setInputMessage(e.target.value)}
          placeholder="Enter message"
        />
        <button onClick={sendMessage}>Send</button>
      </div>
      <div>
        <h2>Messages:</h2>
        {messages.map((message, index) => (
          <p key={index}>{message}</p>
        ))}
      </div>
    </div>
  )
}

export default App
