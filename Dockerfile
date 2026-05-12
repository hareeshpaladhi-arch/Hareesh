FROM ubuntu:22.04

WORKDIR /app

# Copy repository contents
COPY . .

# Set default command
CMD ["/bin/bash"]
