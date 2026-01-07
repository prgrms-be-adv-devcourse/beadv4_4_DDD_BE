git-setup:
	@echo "Setting git commit template for this repository..."
	git config commit.template .gitmessage.txt
	@echo "Done. (repo-local git commit template applied)"
