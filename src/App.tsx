import { useState } from "react"

export default function App(){
  const [chat,setChat] = useState([{u:"MzApps",m:"Ka nung tawh e boss! 🔥"}])
  const [text,setText] = useState("")
  const send = () => {
    if(!text) return
    setChat([...chat,{u:"Nang",m:text}])
    setText("")
    setTimeout(()=>setChat(c=>[...c,{u:"MzApps",m:"Thawn thei em? Nung tawh!"}]),600)
  }
  return(
    <div style={{background:"#000",color:"#fff",height:"100vh",display:"flex",flexDirection:"column",fontFamily:"sans-serif"}}>
      <div style={{padding:16,fontWeight:900,borderBottom:"1px solid #222"}}>MzApps FINAL ✅</div>
      <div style={{flex:1,padding:12,overflow:"auto"}}>
        {chat.map((c,i)=><div key={i} style={{background:i%2?"#fff":"#222",color:i%2?"#000":"#fff",padding:10,borderRadius:12,marginBottom:8,maxWidth:"80%"}}><b>{c.u}</b><div>{c.m}</div></div>)}
      </div>
      <div style={{display:"flex",padding:12,gap:8,borderTop:"1px solid #222"}}>
        <input value={text} onChange={e=>setText(e.target.value)} onKeyDown={e=>e.key==="Enter"&&send()} placeholder="Message type rawh..." style={{flex:1,padding:12,borderRadius:20,border:"none",background:"#222",color:"#fff"}}/>
        <button onClick={send} style={{padding:"12px 18px",borderRadius:20,border:"none",background:"#fff",fontWeight:800}}>Send</button>
      </div>
    </div>
  )
}
