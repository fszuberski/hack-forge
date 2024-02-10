package main

import (
	"fmt"
	"log"
	"os"
)

// Lorem Ipsum text generator

func loremIpsum() string {
	return "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore \n" +
		"magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo \n" +
		"consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. \n" +
		"Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. \n\n"
}

const bytesToGenerate = 100 * 1024 * 1024 // 100mb

func main() {
	// Define the size of the text file in bytes (100MB = 100 * 1024 * 1024 bytes)
	fmt.Printf("Size in bytes to generate: %d \n", bytesToGenerate)

	f, err := os.OpenFile("lorem-ipsum.txt", os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0644)
	if err != nil {
		log.Fatal(err)
	}

	var iteration int64
	appendedBytes := 0
	for appendedBytes < bytesToGenerate {
		if _, err := f.Write([]byte(loremIpsum())); err != nil {
			log.Fatal(err)
		}
		appendedBytes += len(loremIpsum())
		iteration++

		if iteration%20000 == 0 {
			fmt.Printf("Iteraton %d, size in bytes: %d, completed %d percent\n", iteration, appendedBytes, (appendedBytes*100)/bytesToGenerate)
		}
	}

	if err := f.Close(); err != nil {
		log.Fatal(err)
	}

	fmt.Println("Lorem Ipsum text file created successfully.")
}
