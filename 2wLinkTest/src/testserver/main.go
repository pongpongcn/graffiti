// testserver project main.go
package main

import (
	"fmt"
	"log"
	"net"
	"os"
	"strconv"
	"sync"
	"time"

	"github.com/apsdehal/go-logger"
)

var (
	counter int
	mu      sync.Mutex
)

func DoConnection(conn net.Conn, defaultdatalen int) (err1 error) {
	mu.Lock()
	counter++
	mu.Unlock()
	defer DoClose(conn)

	if counter > 60020 {
		return nil
	}

	//senddata := make([]byte, 200)
	senddata := make([]byte, 0, defaultdatalen)
	for j := 0; j < defaultdatalen-1; j++ {
		senddata = append(senddata, byte('a'))
	}
	senddata = append(senddata, byte('\n'))

	cover := make([]byte, 0, 40)
	for j := 0; j < 40; j++ {
		cover = append(cover, byte('a'))
	}

	seq := 1

	for {

		//设置超时
		nowtime := time.Now().Format("2006-01-02 15:04:05.000")
		seqstr := fmt.Sprintf("%d", seq)
		seq++
		tmp := nowtime + "," + seqstr + ","
		copy(senddata, tmp)

		conn.SetDeadline(time.Now().Add(time.Millisecond * 500))
		_, err := conn.Write(senddata)
		conn.SetDeadline(time.Time{})
		if err != nil {
			if nerr, ok := err.(net.Error); !ok || nerr.Timeout() { //timeout
				//fmt.Println("Timeout")
				//t.Fatalf("#%d: %v", i, err)
			} else {
				//fmt.Println("send err, return")
				return
			}
		}
		copy(senddata, cover)
		time.Sleep(time.Second * 1) //one second one packet
	}
	//conn.Close()
	return nil
}

func DoClose(conn net.Conn) (err1 error) {
	mu.Lock()
	counter--
	mu.Unlock()
	return conn.Close()
}

func DoPrintState(lg *logger.Logger) (err1 error) {
	for {
		fmt.Println("connections:", counter)
		time.Sleep(time.Second * 10)
		lg.Debug(fmt.Sprintf("connections:%d", counter))
	}

	// Critically log critical
	//log.Critical("This is Critical!")
	// Debug
	//log.Debug("This is Debug!")
	// Give the Warning
	//log.Warning("This is Warning!")
	// Show the error
	//log.Error("This is Error!")
	// Notice
	//log.Notice("This is Notice!")
	// Show the info
	//log.Info("This is Info!")

	return nil
}

//func DoTest(l net.Listener) (err error) {
//	time.Sleep(time.Second * 10)
//	fmt.Println("stop listen")
//	l.Close()
//	return nil
//}

func main() {
	counter = 0

	f, err := os.Create("server.log") //Create
	if err != nil {
		fmt.Println("Create File Failed:", err)
		return
	}
	defer f.Close()

	lg, err := logger.New("server.log", 4, f) //os.Stdout
	if err != nil {
		panic(err) // Check for error
	}

	defaultdatalen := 200
	defaultport := "10086"

	fmt.Println("useage:testserver port datalen default(10086 200)")

	if len(os.Args) > 1 {
		defaultport = os.Args[1]
		if len(os.Args) > 2 {
			tmplen, _ := strconv.Atoi(os.Args[2])
			if tmplen < 40 || tmplen < 0 {
				lg.Debug("datalen must set more than 40")
			} else {
				defaultdatalen = tmplen
			}
		}
	}
	fmt.Println("defaultport", defaultport, " defaultdatalen", defaultdatalen)

	//ln, err := net.Listen("tcp4", ":10086")
	ln, err := net.Listen("tcp4", ":"+defaultport)
	if err != nil {
		panic(err)
	}

	go DoPrintState(lg)
	//go DoTest(ln)

	for {
		conn, err := ln.Accept()
		if err != nil {
			log.Fatal("get client connection error: ", err)
			//if err == net.errClosing {

			//}
		}
		go DoConnection(conn, defaultdatalen)
	}
}
