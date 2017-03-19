// testclient project main.go
package main

import (
	"bufio"
	"fmt"
	"io"
	"net"
	"os"
	"time"
)

func DoConnection(addr string) {
	//conn, err := net.Dial("tcp", "127.0.0.1:6010")
	//conn, err := net.Dial("tcp", "172.18.194.124:6010")
	conn, err := net.Dial("tcp", addr)

	if err != nil {
		//fmt.Println("Dial error, return")
		return
		//panic(err)
	}
	defer conn.Close()

	buf := make([]byte, 450)

	for {

		//设置超时
		conn.SetDeadline(time.Now().Add(time.Millisecond * 10))
		_, err := bufio.NewReader(conn).Read(buf)
		conn.SetDeadline(time.Time{})

		if err != nil {
			if err == io.EOF {
				fmt.Println("io.EOF,return")
				//err1 = nil
				//fmt.Println("read err, return")
				return
			}

			if nerr, ok := err.(net.Error); !ok || nerr.Timeout() {
				//fmt.Println("Timeout")
			}
		}

		//fmt.Println(data)

		//time.Sleep(time.Second * 2)
	}

	return
}

func main() {
	defaultaddr := "127.0.0.1"
	defaultport := "10086"

	if len(os.Args) > 1 {
		defaultaddr = os.Args[1]
		if len(os.Args) > 2 {
			defaultport = os.Args[2]
		}
	}
	fmt.Println("defaultaddr", defaultaddr, " defaultport", defaultport)

	//fmt.Println("Hello World!")
	conncounter := 5000
	for i := 0; i < conncounter; i++ {
		if i%100 == 0 {
			time.Sleep(time.Millisecond * 1000)
			fmt.Println("sleep", time.Now(), "connections:", i)
		}
		//time.Sleep(time.Millisecond * 15)
		//go DoConnection("127.0.0.1:6010")
		go DoConnection(defaultaddr + ":" + defaultport)
	}
	fmt.Printf("connection:%d success", conncounter)

	for {
		time.Sleep(time.Second * 30)
	}
}
