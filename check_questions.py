import json

with open('questions.json', 'r', encoding='utf-8') as f:
    data = json.load(f)

categories = {}
for q in data:
    cat = q['category']
    diff = q['difficulty']
    if cat not in categories:
        categories[cat] = {}
    if diff not in categories[cat]:
        categories[cat][diff] = 0
    categories[cat][diff] += 1

print("\nCategorii distincte:")
for cat in sorted(categories.keys()):
    print(f"  - {cat}")

print(f"\nTotal categorii: {len(categories)}")
print("\nDistributie completa (minim necesar: 3):")
print(f"{'Categorie':<20} {'EASY':<8} {'MEDIUM':<8} {'HARD':<8}")
print("-" * 50)

insufficient = []
for cat in sorted(categories.keys()):
    easy = categories[cat].get('EASY', 0)
    medium = categories[cat].get('MEDIUM', 0)
    hard = categories[cat].get('HARD', 0)
    print(f"{cat:<20} {easy:<8} {medium:<8} {hard:<8}")
    
    if easy < 3:
        insufficient.append(f"{cat} EASY ({easy}/3)")
    if medium < 3:
        insufficient.append(f"{cat} MEDIUM ({medium}/3)")
    if hard < 3:
        insufficient.append(f"{cat} HARD ({hard}/3)")

print(f"\nTotal intrebari: {len(data)}")

if insufficient:
    print("\n❌ Combinatii insuficiente (<3 intrebari):")
    for combo in insufficient:
        print(f"  - {combo}")
    print(f"\nTotal combinatii insuficiente: {len(insufficient)}")
else:
    print("\n✓ Toate combinatiile au minim 3 intrebari pentru fiecare dificultate!")
