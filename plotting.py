import matplotlib.pyplot as plt

with open("roulette_bigMoney.txt") as file:
	avg = []
	genSum = 0
	count = 0

	probs = []
	cards = []
	best = -100
	genProbs = []
	genCards = []

	for line in file:
		words = line.split()
		if words[0] == "Generation":
			if count > 0:
				avg.append(genSum / count)
			
			gen = words[1]
			genSum = 0
			count = 0
			best = -100
			probs.append(genProbs)
			cards.append(genCards)
		else:
			genSum += float(words[0])
			count += 1
			if float(words[0]) > best:
				best = float(words[0])
				genProbs = []
				genCards = []
				for word in words:
					prob = word.split(":")
					if len(prob) == 2:
						genCards.append(prob[0])
						genProbs.append(prob[1])




	avg.append(genSum / count)
	probs.append(genProbs)
	cards.append(genCards)

	fig, axs = plt.subplots(2, 5)

	axs[0,0].plot(avg, "o")
#	axs[0].xlabel("Generation")
#	axs[0].ylabel("Average Amount Won By")
#	axs[0].title("Genetic Algorithm AI for Big Money")
#	axs[0,0].pie(probs[0], labels=cards[0])

	for i in range(1, 10):
		axs[i//5, i%5].pie(probs[i], labels=cards[i])
#	axs[1].pie(probs, labels=cards)
	axs[0,0].plot(avg, "o")

	plt.show()
	print(avg)
