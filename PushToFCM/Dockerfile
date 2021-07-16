FROM node:16
WORKDIR /usr/src/app

COPY package*.json ./
RUN npm install --only=production
COPY . .
EXPOSE 3000
CMD ["node", "index.js"]
HEALTHCHECK --interval=5s --timeout=2s --start-period=5s --retries=3 CMD curl -fSsO http://localhost:3000/health || exit 1